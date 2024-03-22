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
import kotlin.test.assertIs
import kotlin.test.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@FlowExtPreview
@ExperimentalCoroutinesApi
class ErrorsTest : BaseTest() {
  @Test
  fun testCatchAndReturnItem_emitsFallback() = runTest {
    val testException = TestException()

    flow {
      emit(1)
      throw testException
    }
      .catchAndReturn(2)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testCatchAndReturnItem_successCase() = runTest {
    flowOf(1, 2, 3)
      .catchAndReturn(42)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testCatchAndReturnWithLambda_emitsFallback() = runTest {
    var count = 2

    val testException = TestException()

    val flow = flow {
      emit(1)
      throw testException
    }
      .catchAndReturn {
        assertIs<TestException>(it)
        delay(10)
        count++
      }

    flow.test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Complete,
      ),
      null,
    )
    flow.test(
      listOf(
        Event.Value(1),
        Event.Value(3),
        Event.Complete,
      ),
      null,
    )
  }

  @Test
  fun testCatchAndReturnWithLambda_successCase() = runTest {
    val flow = flowOf(1, 2, 3)
      .catchAndReturn {
        delay(10)
        fail("Should be unreached")
      }

    flow.test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
      null,
    )
  }

  @Test
  fun testCatchAndResumeFlow_emitsFallback() = runTest {
    val testException = TestException()
    var count = 2

    val flow = flow {
      emit(1)
      throw testException
    }.catchAndResume(defer { flowOf(count, count + 1).also { count++ } })

    flow
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
        null,
      )

    flow
      .test(
        listOf(
          Event.Value(1),
          Event.Value(3),
          Event.Value(4),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testCatchAndResumeFlow_successCase() = runTest {
    val flow = flowOf(1, 2, 3)
      .catchAndResume(flow { fail("Should be unreached") })

    flow.test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
      null,
    )
  }

  @Test
  fun testCatchAndResumeWithLambda_emitsFallback() = runTest {
    val testException = TestException()
    var count = 2

    val flow = flow {
      emit(1)
      throw testException
    }.catchAndResume {
      assertIs<TestException>(it)
      delay(10)
      flowOf(count, count + 1).also { count++ }
    }

    flow
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
        null,
      )

    flow
      .test(
        listOf(
          Event.Value(1),
          Event.Value(3),
          Event.Value(4),
          Event.Complete,
        ),
        null,
      )
  }

  @Test
  fun testCatchAndResumeWithLambda_successCase() = runTest {
    val flow = flowOf(1, 2, 3)
      .catchAndResume {
        delay(10)
        fail("Should be unreached")
      }

    flow.test(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
      null,
    )
  }
}
