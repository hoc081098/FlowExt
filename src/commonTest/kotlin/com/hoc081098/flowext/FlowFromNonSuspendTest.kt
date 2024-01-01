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
import com.hoc081098.flowext.utils.NamedDispatchers
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take

@ExperimentalCoroutinesApi
class FlowFromNonSuspendTest : BaseTest() {
  @Test
  fun flowFromNonSuspendEmitsValues() = runTest {
    var count = 0L
    val flow = flowFromNonSuspend {
      count
    }

    flow.test(listOf(Event.Value(0L), Event.Complete))

    ++count
    flow.test(listOf(Event.Value(1L), Event.Complete))

    ++count
    flow.test(listOf(Event.Value(2L), Event.Complete))
  }

  @Test
  fun flowFromNonSuspendFunctionThrows() = runTest {
    val testException = TestException()

    flowFromNonSuspend<Int> { throw testException }.test(listOf(Event.Error(testException)))
  }

  @Test
  fun flowFromNonSuspendCancellation() = runTest {
    fun throwsCancellationException(): Unit = throw CancellationException("Flow was cancelled")
    assertFailsWith<CancellationException>(
      flowFromNonSuspend {
        val i = 1 + 2
        throwsCancellationException()
        i + 3
      },
    )
  }

  @Test
  fun flowFromNonSuspenddTake() = runTest {
    flowFromNonSuspend { 100 }
      .take(1)
      .test(listOf(Event.Value(100), Event.Complete))
  }

  @Test
  fun testContextPreservation1() = runTest {
    val flow = flowFromNonSuspend {
      assertEquals("OK", NamedDispatchers.name())
      42
    }.flowOn(NamedDispatchers("OK"))

    flow.test(listOf(Event.Value(42), Event.Complete))
  }

  @Test
  fun testCancellable() = runTest {
    var sum = 0
    val flow = flowFromNonSuspend { 1 }
      .onStart { currentCoroutineContext().cancel() }
      .onEach { sum += it }

    flow.launchIn(this).join()
    assertEquals(0, sum)

    sum = 0
    flow.cancellable().launchIn(this).join()
    assertEquals(0, sum)
  }
}
