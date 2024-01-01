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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MapToTest : BaseTest() {
  @Test
  fun basic() = runTest {
    val values = flowOf(1, 2, 3).mapTo(4).toList()
    assertEquals(values, listOf(4, 4, 4))
  }

  @Test
  fun mapToUnitBasic() = runTest {
    (0 until 10).asFlow()
      .mapToUnit()
      .test(List(10) { Event.Value(Unit) } + Event.Complete)
  }

  @Test
  fun upstreamError() = runTest {
    val throwable = TestException()

    flow<Nothing> { throw throwable }
      .mapTo(2)
      .test(listOf(Event.Error(throwable)))
  }

  @Test
  fun mapToWithFailureUpstream() = runTest {
    val throwable = TestException()

    flow<Nothing> { throw throwable }
      .mapToUnit()
      .test(listOf(Event.Error(throwable)))
  }
}
