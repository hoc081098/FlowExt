/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

fun <T> Iterable<T>.cycled(): Sequence<T> = sequence {
  while (true) {
    yieldAll(this@cycled)
  }
}

@FlowExtPreview
@ExperimentalCoroutinesApi
class RepeatForeverTest : BaseTest() {
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
          Event.Complete
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
          Event.Complete
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
          Event.Complete
      )
  }
}
