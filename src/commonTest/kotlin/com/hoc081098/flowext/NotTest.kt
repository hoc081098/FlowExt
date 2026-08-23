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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class NotTest : BaseTest() {
  @Test
  fun notBasic() = runTest {
    val values = flowOf(true, false, true, false).not().toList()
    assertEquals(listOf(false, true, false, true), values)
  }

  @Test
  fun notEmpty() = runTest {
    val values = flowOf<Boolean>().not().toList()
    assertEquals(emptyList(), values)
  }

  @Test
  fun notSingleTrue() = runTest {
    val values = flowOf(true).not().toList()
    assertEquals(listOf(false), values)
  }

  @Test
  fun notSingleFalse() = runTest {
    val values = flowOf(false).not().toList()
    assertEquals(listOf(true), values)
  }

  @Test
  fun notUpstreamError() = runTest {
    val throwable = TestException()

    flow<Boolean> { throw throwable }
      .not()
      .test(listOf(Event.Error(throwable)))
  }

  @Test
  fun notSequence() = runTest {
    listOf(true, false, true, false, true)
      .asFlow()
      .not()
      .test(
        listOf(
          Event.Value(false),
          Event.Value(true),
          Event.Value(false),
          Event.Value(true),
          Event.Value(false),
          Event.Complete,
        ),
      )
  }

  @Test
  fun notPropagatesCancellationToUpstream() = runTest {
    val upstreamStarted = CompletableDeferred<Unit>()
    val upstreamCompletion = CompletableDeferred<Throwable?>()

    val job = flow<Boolean> {
      upstreamStarted.complete(Unit)
      awaitCancellation()
    }
      .onCompletion { upstreamCompletion.complete(it) }
      .not()
      .launchIn(this)

    upstreamStarted.await()
    job.cancelAndJoin()

    assertIs<CancellationException>(upstreamCompletion.await())
  }
}
