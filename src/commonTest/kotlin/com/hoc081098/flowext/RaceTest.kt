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
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take

/*
 * MIT License
 *
 * Copyright (c) 2021 Petrus Nguyễn Thái Học
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

@ExperimentalCoroutinesApi
class RaceTest : BaseTest() {
  @Test
  fun raceZero() = runTest {
    race(emptyList<Flow<Int>>()).test(listOf(Event.Complete))
  }

  @Test
  fun raceSingle() = runTest {
    race(
      listOf(
        flowOf(1, 2, 3),
      ),
    ).test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
    )
  }

  @Test
  fun race2WithoutDelay() = runTest {
    race(
      flowOf(1, 2).log(1),
      flowOf(3, 4).log(2),
    ).test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Complete,
      ),
    )
  }

  @Test
  fun race2WithDelay() = runTest {
    race(
      flowOf(1, 2).onEach { delay(200) }.log(1),
      flowOf(3, 4).onEach { delay(100) }.log(2),
      flowOf(5, 6).onEach { delay(50) }.log(3),
    ).test(
      listOf(
        Event.Value(5),
        Event.Value(6),
        Event.Complete,
      ),
    )
  }

  @Test
  fun basic() = runTest {
    race(
      flow {
        delay(100)
        emit(1)
        emit(2)
        emit(3)
      },
      flow {
        delay(200)
        emit(2)
        emit(3)
        emit(4)
      },
    ).test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
    )
  }

  @Test
  fun raceComplete() = runTest {
    race(
      flow {
        delay(100)
      },
      flow {
        delay(200)
        emit(1)
      },
    ).test(listOf(Event.Complete))

    race(
      flow {
        delay(200)
        emit(1)
      },
      flow {
        delay(100)
      },
    ).test(listOf(Event.Complete))

    race(
      flow {
        delay(200)
        emit(1)
      },
      emptyFlow(),
    ).test(listOf(Event.Complete))
  }

  @Test
  fun raceFailureUpstream() = runTest {
    race(
      flow {
        delay(100)
        emit(1)
        delay(500)
        throw TestException()
      },
      flow {
        delay(500)
        emit(2)
        emit(500)
        emit(4)
      },
    ).test(null) { events ->
      assertEquals(2, events.size)
      val (a, b) = events

      assertEquals(1, a.valueOrThrow())
      assertIs<TestException>(b.errorOrThrow())
    }

    race(
      flow {
        delay(1000)
        emit(1)
        delay(500)
      },
      flow {
        delay(500)
        throw TestException()
      },
    ).test(null) { events ->
      assertIs<TestException>(events.single().errorOrThrow())
    }
  }

  @Test
  fun raceTake() = runTest {
    race(flowOf(1).log(1), flowOf(2).log(2))
      .take(1)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        ),
      )

    race(
      flow {
        delay(100)
        emit(1)
        throw TestException("")
      }.log(1),
      flow {
        delay(200)
        emit(2)
      }.log(2),
    )
      .take(1)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        ),
      )
  }

  @Test
  fun raceCancellation() = runTest {
    val message = "test"

    race(
      flow {
        delay(50)
        throw CancellationException(message)
      },
      flow {
        delay(100)
        emit(1)
      },
    ).test(null) { events ->
      assertEquals(
        message,
        assertIs<CancellationException>(events.single().errorOrThrow()).message,
      )
    }
  }
}

private fun <T> Flow<T>.log(tag: Any) = onEach { println("[$tag] >>> $it") }
  .onStart { println("[$tag] start") }
  .onCompletion { println("[$tag] complete $it") }
