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

import com.hoc081098.flowext.internal.AtomicBoolean
import com.hoc081098.flowext.utils.BaseTest
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

@ExperimentalCoroutinesApi
class TimerTest : BaseTest() {
  @Test
  fun timer() = runTest {
    timer(1, 1.seconds)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        ),
      )

    timer(2, 1_000)
      .test(
        listOf(
          Event.Value(2),
          Event.Complete,
        ),
      )
  }

  @Test
  fun timerLong() = runTest {
    val emitted = AtomicBoolean()

    launch {
      timer(1, 2_000).collect {
        assertEquals(1, it)
        emitted.compareAndSet(expect = false, update = true)
      }
    }

    runCurrent()
    assertEquals(false, emitted.value)

    advanceTimeBy(1_000)
    assertEquals(false, emitted.value)

    advanceTimeBy(1_001)
    assertEquals(true, emitted.value)
  }

  @Test
  fun timerDuration() = runTest {
    val emitted = AtomicBoolean()

    launch {
      timer(2, 2.seconds).collect {
        assertEquals(2, it)
        emitted.compareAndSet(expect = false, update = true)
      }
    }

    runCurrent()
    assertEquals(false, emitted.value)

    advanceTimeBy(1_000)
    assertEquals(false, emitted.value)

    advanceTimeBy(1_001)
    assertEquals(true, emitted.value)
  }
}
