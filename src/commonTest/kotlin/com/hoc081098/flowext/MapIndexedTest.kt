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
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MapIndexedTest : BaseTest() {
  @Test
  fun basic() = runTest {
    flowOf(1, 2, 3, 4)
      .mapIndexed { index, value -> index to value }
      .test(
        listOf(
          Event.Value(0 to 1),
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Value(3 to 4),
          Event.Complete,
        ),
      )
  }

  @Test
  fun upstreamError() = runTest {
    val throwable = TestException()

    flow<Int> { throw throwable }
      .mapIndexed { index, value -> index to value }
      .test(listOf(Event.Error(throwable)))
  }

  @Test
  fun cancellation() = runTest {
    flow {
      repeat(5) {
        if (it == 2) {
          throw CancellationException("")
        } else {
          emit(it)
        }
      }
    }
      .mapIndexed { index, value -> index to value }
      .test(null) {
        assertEquals(
          listOf(
            Event.Value(0 to 0),
            Event.Value(1 to 1),
          ),
          it.take(2),
        )
        assertIs<CancellationException>(it.drop(2).single().errorOrThrow())
      }

    flowOf(0, 1, 2, 3, 4, 5, 6)
      .mapIndexed { index, value -> index to value }
      .take(2)
      .test(
        listOf(
          Event.Value(0 to 0),
          Event.Value(1 to 1),
          Event.Complete,
        ),
      )
  }
}
