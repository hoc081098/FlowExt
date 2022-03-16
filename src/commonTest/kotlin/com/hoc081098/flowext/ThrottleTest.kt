/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hoc081098.flowext

import com.hoc081098.flowext.ThrottleConfiguration.TRAILING
import com.hoc081098.flowext.utils.BaseTest
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class ThrottleFirstTest : BaseTest() {
  @Test
  fun throttleWithCompleted_A() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .throttleTime(500)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(4),
          Event.Value(7),
          Event.Value(10),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleWithCompleted_B() = runTest {
    flow {
      emit(1) // deliver
      emit(2) // skip
      delay(501) // 501

      emit(3) // deliver
      delay(99) // 600

      emit(4) // skip
      delay(100) // 700

      emit(5) // skip
      emit(6) // skip
      delay(301) // 1001

      emit(7) // deliver
      delay(400) // 1501
    }
      .throttleTime(500)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(3),
          Event.Value(7),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleWithCompletedAndNotDelay_A() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .throttleTime(0)
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleWithCompletedAndNotDelay_B() = runTest {
    (1..10)
      .asFlow()
      .throttleTime(0)
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleWithCompletedAndNotDelay_C() = runTest {
    (1..10)
      .asFlow()
      .throttle { emptyFlow() }
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleNullableWithCompleted() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .map { v -> v.takeIf { it % 2 == 0 } }
      .throttleTime(500)
      .test(
        listOf(
          Event.Value(null),
          Event.Value(4),
          Event.Value(null),
          Event.Value(10),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleSingleFlow() = runTest {
    flowOf(1)
      .throttleTime(100)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleEmptyFlow() = runTest {
    emptyFlow<Int>()
      .throttleTime(100)
      .test(listOf(Event.Complete))
  }

  @Test
  fun throttleNeverFlow() = runTest {
    var hasValue = false

    val job = neverFlow()
      .throttleTime(100)
      .onEach { hasValue = true }
      .launchIn(this)
    advanceTimeBy(1000)
    job.cancel()

    assertFalse(hasValue)
  }

  @Test
  fun throttleFailureUpstream() = runTest {
    flow {
      emit(1)
      throw TestException("Broken!")
    }.throttleTime(200).test(null) {
      assertIs<TestException>(it.single().errorOrThrow())
    }

    flow {
      emit(1)
      delay(500)
      emit(2)
      delay(500)
      throw TestException("Broken!")
    }.throttleTime(200).test(null) { events ->
      assertEquals(3, events.size)
      val (a, b, c) = events

      assertEquals(1, a.valueOrThrow())
      assertEquals(2, b.valueOrThrow())
      assertIs<TestException>(c.errorOrThrow())
    }

    flow {
      emit(1) // Should be published since it is first
      delay(100)
      emit(2) // Should be skipped since error will arrive before the timeout expires
      delay(100) // Should be published as soon as the timeout expires.
      throw TestException("Broken!")
    }.throttleTime(400).test(null) { events ->
      assertEquals(2, events.size)
      val (a, b) = events

      assertEquals(1, a.valueOrThrow())
      assertIs<TestException>(b.errorOrThrow())
    }
  }

  @Test
  fun throttleFailureSelector() = runTest {
    (1..10)
      .asFlow()
      .throttle { throw TestException("Broken!") }
      .test(null) { events ->
        assertEquals(2, events.size)
        val (a, b) = events

        assertEquals(1, a.valueOrThrow())
        assertIs<TestException>(b.errorOrThrow())
      }

    flow {
      emit(1)
      delay(100)
      emit(2)
      delay(400)
      emit(3)
    }
      .throttle {
        when (it) {
          1 -> timer(Unit, 400)
          3 -> flow { throw TestException("1") }
          else -> flow { throw TestException("2") }
        }
      }
      .test(null) { events ->
        assertEquals(3, events.size)
        val (a, b, c) = events

        assertEquals(1, a.valueOrThrow())
        assertEquals(3, b.valueOrThrow())
        assertEquals(
          "1",
          assertIs<TestException>(c.errorOrThrow()).message
        )
      }

    flow {
      emit(1)
      delay(100)
      emit(2)
      delay(400)
      emit(3)
      delay(600)
      emit(4)
    }
      .throttle {
        when (it) {
          1 -> timer(Unit, 400)
          3 -> flow {
            delay(500)
            throw TestException("1")
          }
          else -> flow { throw TestException("2") }
        }
      }
      .test(null) { events ->
        assertEquals(3, events.size)
        val (a, b, c) = events

        assertEquals(1, a.valueOrThrow())
        assertEquals(3, b.valueOrThrow())
        assertEquals(
          "1",
          assertIs<TestException>(c.errorOrThrow()).message
        )
      }
  }

  @Test
  fun throttleTake() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .throttleTime(500)
      .take(1)
      .test(listOf(Event.Value(1), Event.Complete))

    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .concatWith(flow { throw TestException() })
      .throttleTime(500)
      .take(1)
      .test(listOf(Event.Value(1), Event.Complete))
  }

  @Test
  fun throttleCancellation() = runTest {
    var count = 0
    (1..10).asFlow()
      .onEach { delay(200) }
      .throttle {
        if (count++ % 2 == 0) {
          flow { throw CancellationException("") }
        } else {
          timer(Unit, 500)
        }
      }
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(5),
          Event.Value(6),
          Event.Value(9),
          Event.Value(10),
          Event.Complete,
        )
      )
  }
}

@ExperimentalCoroutinesApi
class ThrottleLastTest : BaseTest() {
  @Test
  fun throttleWithCompleted_A() = runTest {
    flow {
      // -1---2----3-
      // -@-----!--@-----!
      // -------2--------3

      delay(100)
      emit(1)
      delay(300)
      emit(2)
      delay(400)
      emit(3)
      delay(100)
    }
      .throttleTime(500, TRAILING)
      .test(
        listOf(
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleWithCompleted_B() = runTest {
    flow {
      // -1---2----3----4
      // -@-----!--@-----!
      // -------2--------4

      delay(100)
      emit(1)
      delay(300)
      emit(2)
      delay(400)
      emit(3)
      delay(450)
      emit(4)
    }
      .throttleTime(500, TRAILING)
      .test(
        listOf(
          Event.Value(2),
          Event.Value(4),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleWithCompleted_C() = runTest {
    flow {
      // -1---2----3------4|
      // -@-----!--@-----!
      // -------2--------3 4

      delay(100)
      emit(1)
      delay(300)
      emit(2)
      delay(400)
      emit(3)
      delay(550)
      emit(4)
    }
      .throttleTime(500, TRAILING)
      .test(
        listOf(
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleWithCompletedAndNotDelay_A() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .throttleTime(0, TRAILING)
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleWithCompletedAndNotDelay_B() = runTest {
    (1..10)
      .asFlow()
      .throttleTime(0, TRAILING)
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleWithCompletedAndNotDelay_C() = runTest {
    (1..10)
      .asFlow()
      .throttle(TRAILING) { emptyFlow() }
      .test((1..10).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun throttleNullableWithCompleted() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .map { v -> v.takeIf { it % 2 == 0 } }
      .throttleTime(500, TRAILING)
      .test(
        listOf(
          Event.Value(null),
          Event.Value(6),
          Event.Value(null),
          Event.Value(10),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleSingleFlow() = runTest {
    flowOf(1)
      .throttleTime(100, TRAILING)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        )
      )
  }

  @Test
  fun throttleEmptyFlow() = runTest {
    emptyFlow<Int>()
      .throttleTime(100, TRAILING)
      .test(listOf(Event.Complete))
  }

  @Test
  fun throttleNeverFlow() = runTest {
    var hasValue = false

    val job = neverFlow()
      .throttleTime(100, TRAILING)
      .onEach { hasValue = true }
      .launchIn(this)
    advanceTimeBy(1000)
    job.cancel()

    assertFalse(hasValue)
  }

  @Test
  fun throttleFailureUpstream() = runTest {
    flow {
      emit(1)
      throw TestException("Broken!")
    }.throttleTime(200, TRAILING).test(null) {
      assertIs<TestException>(it.single().errorOrThrow())
    }

    flow {
      // 1-----2----X
      //  --1   --2

      emit(1)
      delay(500)
      emit(2)
      delay(500)
      throw TestException("Broken!")
    }.throttleTime(200, TRAILING).test(null) { events ->
      assertEquals(3, events.size)
      val (a, b, c) = events

      assertEquals(1, a.valueOrThrow())
      assertEquals(2, b.valueOrThrow())
      assertIs<TestException>(c.errorOrThrow())
    }

    flow {
      // 1-2-X
      //  ----X

      emit(1)
      delay(100)
      emit(2)
      delay(100)
      throw TestException("Broken!")
    }.throttleTime(400, TRAILING).test(null) { events ->
      assertIs<TestException>(events.single().errorOrThrow())
    }
  }

  @Test
  fun throttleFailureSelector() = runTest {
    (1..10)
      .asFlow()
      .throttle(TRAILING) { throw TestException("Broken!") }
      .test(null) { events ->
        assertIs<TestException>(events.single().errorOrThrow())
      }

    flow {
      // 1-2----3
      //  ----2 X

      emit(1)
      delay(100)
      emit(2)
      delay(400)
      emit(3)
    }
      .throttle(TRAILING) {
        when (it) {
          1 -> timer(Unit, 400)
          3 -> flow { throw TestException("1") }
          else -> flow { throw TestException("2") }
        }
      }
      .test(null) { events ->
        assertEquals(2, events.size)
        val (a, b) = events

        assertEquals(2, a.valueOrThrow())
        assertEquals(
          "1",
          assertIs<TestException>(b.errorOrThrow()).message
        )
      }

    flow {
      // 1-2----3------4
      //  ----2  -----X

      emit(1)
      delay(100)
      emit(2)
      delay(400)
      emit(3)
      delay(600)
      emit(4)
    }
      .throttle(TRAILING) {
        when (it) {
          1 -> timer(Unit, 400)
          3 -> flow {
            delay(500)
            throw TestException("1")
          }
          else -> flow { throw TestException("2") }
        }
      }
      .test(null) { events ->
        assertEquals(2, events.size)
        val (a, b) = events

        assertEquals(2, a.valueOrThrow())
        assertEquals(
          "1",
          assertIs<TestException>(b.errorOrThrow()).message
        )
      }
  }

  @Test
  fun throttleTake() = runTest {
    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .throttleTime(500, TRAILING)
      .take(1)
      .test(listOf(Event.Value(3), Event.Complete))

    (1..10)
      .asFlow()
      .onEach { delay(200) }
      .concatWith(flow { throw TestException() })
      .throttleTime(500, TRAILING)
      .take(1)
      .test(listOf(Event.Value(3), Event.Complete))
  }

  @Test
  fun throttleCancellation() = runTest {
    // --1--2--3--4--5--6--7--8--9--10

    var count = 0
    (1..10).asFlow()
      .onEach { delay(200) }
      .throttle(TRAILING) {
        if (count++ % 2 == 0) {
          flow { throw CancellationException("") }
        } else {
          timer(Unit, 500)
        }
      }
      .test(
        listOf(
          Event.Value(1),
          Event.Value(4),
          Event.Value(5),
          Event.Value(8),
          Event.Value(9),
          Event.Value(10),
          Event.Complete,
        )
      )
  }
}
