/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Petrus Nguyễn Thái Học
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

import com.hoc081098.flowext.utils.BaseTest
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

fun <T> Iterable<T>.cycled(): Sequence<T> = sequence {
  while (true) {
    yieldAll(this@cycled)
  }
}

@ExperimentalCoroutinesApi
class RepeatForeverTest : BaseTest() {
  @Test
  fun testNeverFlow() = runTest(timeout = 3.seconds) {
    val flow = flow<Int> { delay(1) }.repeat()

    val buffer = mutableListOf<Int>()
    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      flow.toList(buffer)
    }
    val intervalJob = interval(Duration.ZERO, 100.milliseconds)
      .take(1_000)
      .launchIn(this)

    runCurrent()

    repeat(1_000) {
      advanceTimeBy(100)
      runCurrent()
      assertTrue(buffer.isEmpty())
    }

    intervalJob.cancelAndJoin()
    job.cancelAndJoin()

    assertTrue(buffer.isEmpty())
  }

  @Test
  fun repeat() = runTest {
    flowOf(1, 2, 3)
      .repeat()
      .take(100)
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(100)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatWithFixedDelay() = runTest {
    var job: Job? = null
    val delay = 500.milliseconds

    flow {
      assertNull(job, "Job must be null")

      emit(1)
      emit(2)
      emit(3)

      assertNull(job, "Job must be null")
      job = launch {
        delay(delay * 0.99)
        job = null
      }
    }
      .repeat(delay)
      .take(100)
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(100)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatWithDelay() = runTest {
    var job: Job? = null
    var count = 1

    flow {
      assertNull(job, "Job must be null")

      emit(1)
      emit(2)
      emit(3)

      assertNull(job, "Job must be null")
      job = launch {
        delay((count++).milliseconds * 0.99)
        job = null
      }
    }
      .repeat { it.milliseconds }
      .take(100)
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(100)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatFailureFlow() = runTest {
    val flow = flow {
      emit(1)
      throw RuntimeException("Error")
    }

    flow
      .repeat()
      .test(null) { (a, b) ->
        assertEquals(
          expected = Event.Value(1),
          actual = a,
        )
        assertIs<RuntimeException>(
          assertIs<Event.Error>(b).error,
        )
      }
  }
}

@ExperimentalCoroutinesApi
class RepeatAtMostTest : BaseTest() {
  @Test
  fun repeatWithZeroCount() = runTest {
    assertTrue {
      flowOf(1, 2, 3)
        .repeat(count = 0)
        .count() == 0
    }
  }

  @Test
  fun repeatWithNegativeCount() = runTest {
    assertTrue {
      flowOf(1, 2, 3)
        .repeat(count = -1)
        .count() == 0
    }
  }

  @Test
  fun repeat() = runTest {
    flowOf(1, 2, 3)
      .repeat(count = 100)
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(300)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatWithFixedDelay() = runTest {
    var job: Job? = null
    val delay = 500.milliseconds

    flow {
      assertNull(job, "Job must be null")

      emit(1)
      emit(2)
      emit(3)

      assertNull(job, "Job must be null")
      job = launch {
        delay(delay * 0.99)
        job = null
      }
    }
      .repeat(count = 100, delay = delay)
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(300)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatWithDelay() = runTest {
    var job: Job? = null
    var count = 1

    flow {
      assertNull(job, "Job must be null")

      emit(1)
      emit(2)
      emit(3)

      assertNull(job, "Job must be null")
      job = launch {
        delay((count++).milliseconds * 0.99)
        job = null
      }
    }
      .repeat(count = 100) { it.milliseconds }
      .test(
        listOf(1, 2, 3)
          .cycled()
          .take(300)
          .map { Event.Value(it) }
          .toList() +
          Event.Complete,
      )
  }

  @Test
  fun repeatFailureFlow() = runTest {
    val flow = flow {
      emit(1)
      throw RuntimeException("Error")
    }

    flow
      .repeat(count = 100)
      .test(null) { (a, b) ->
        assertEquals(
          expected = Event.Value(1),
          actual = a,
        )
        assertIs<RuntimeException>(
          assertIs<Event.Error>(b).error,
        )
      }
  }
}
