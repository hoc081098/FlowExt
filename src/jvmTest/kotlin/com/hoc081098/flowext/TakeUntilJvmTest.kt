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

import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.test
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking

@Ignore("Ignore JVM tests. Run only locally.")
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class TakeUntilJvmTest {
  @Test
  fun takeUntilSingle() = runBlocking {
    range(0, 10)
      .takeUntil(flowOf(1))
      .test(listOf(Event.Complete))

    flowOf(1)
      .takeUntil(flowOf(1))
      .test(listOf(Event.Complete))
  }

  @Test
  fun sourceCompletesAfterNotifier() = runBlocking {
    range(0, 10)
      .onEach { delay(100) }
      .onCompletion { println(it) }
      .takeUntil(timer(Unit, 470.milliseconds))
      .test(
        listOf(
          Event.Value(0),
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun sourceCompletesBeforeNotifier() = runBlocking {
    range(0, 10)
      .onEach { delay(30) }
      .takeUntil(timer(Unit, 10.seconds))
      .test(
        (0 until 10).map { Event.Value(it) } +
          Event.Complete,
      )
  }

  @Test
  fun upstreamError() = runBlocking {
    flow<Nothing> { throw TestException() }
      .takeUntil(timer(Unit, 100))
      .test(null) {
        assertIs<TestException>(it.single().errorOrThrow())
      }

    flow {
      emit(1)
      throw TestException()
    }
      .takeUntil(timer(Unit, 100))
      .test(null) {
        assertEquals(2, it.size)
        assertEquals(1, it[0].valueOrThrow())
        assertIs<TestException>(it[1].errorOrThrow())
      }
  }

  @Test
  fun take() = runBlocking {
    flowOf(1, 2, 3)
      .takeUntil(timer(Unit, 100))
      .take(1)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        ),
      )
  }
}
