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
import kotlin.test.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@FlowExtPreview
@ExperimentalCoroutinesApi
class ResultsTest : BaseTest() {
  @Test
  fun testMapToResult_emitSuccessValues() = runTest {
    flowOf(1, 2, 3)
      .mapToResult()
      .test(
        listOf(
          Event.Value(Result.success(1)),
          Event.Value(Result.success(2)),
          Event.Value(Result.success(3)),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testMapToResult_emitFailureValues() = runTest {
    val testException = TestException()

    flow {
      emit(1)
      throw testException
    }
      .mapToResult()
      .test(
        listOf(
          Event.Value(Result.success(1)),
          Event.Value(Result.failure(testException)),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testMapResultCatching_emitSuccessValues() = runTest {
    flowOf(Result.success(1), Result.success(2), Result.success(3))
      .mapResultCatching { it * 2 }
      .test(
        listOf(
          Event.Value(Result.success(2)),
          Event.Value(Result.success(4)),
          Event.Value(Result.success(6)),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testMapResultCatching_emitFailureValues() = runTest {
    val testException = TestException()

    flowOf(Result.success(1), Result.success(2), Result.success(3))
      .mapResultCatching { throw testException }
      .test(
        listOf(
          Event.Value(Result.failure(testException)),
          Event.Value(Result.failure(testException)),
          Event.Value(Result.failure(testException)),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testMapResultCatching_forwardFailureValues() = runTest {
    val result = Result.failure<Int>(TestException())

    flowOf(result)
      .mapResultCatching<Int, String> { fail("Should not be called") }
      .test(
        listOf(
          Event.Value(result),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testThrowFailure_successValues() = runTest {
    val values = 1..10
    val successes = values.map { Result.success(it) }

    successes
      .asFlow()
      .throwFailure()
      .test(values.map { Event.Value(it) } + Event.Complete, null)
  }
}
