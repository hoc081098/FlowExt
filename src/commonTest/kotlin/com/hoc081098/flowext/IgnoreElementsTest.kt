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
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

@OptIn(FlowExtPreview::class)
@ExperimentalCoroutinesApi
class IgnoreElementsTest : BaseTest() {
  @Test
  fun notEmitAnyValue() = runTest(timeout = 3.seconds) {
    val flow = interval(0, 1)
      .ignoreElements()

    val buffer = mutableListOf<Int>()
    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      flow.toList(buffer)
    }
    val intervalJob = interval(Duration.ZERO, 100.milliseconds)
      .take(1_000)
      .launchIn(this)

    runCurrent()

    repeat(1_000) {
      advanceTimeBy(100)
      runCurrent()
      assertTrue(buffer.isEmpty())
    }

    intervalJob.cancelAndJoin()
    job.cancelAndJoin()

    assertTrue(buffer.isEmpty())
  }

  @Test
  fun normallyCompleted() = runTest {
    flowOf(1, 2, 3)
      .ignoreElements()
      .test(listOf(Event.Complete))
  }

  @Test
  fun handleEmptyFlow() = runTest {
    emptyFlow<Int>()
      .ignoreElements()
      .test(listOf(Event.Complete))
  }

  @Test
  fun handleFailureUpstream() = runTest {
    assertFailsWith<TestException>(
      flow<Int> { throw TestException() }
        .ignoreElements(),
    )

    assertFailsWith<TestException>(
      flow {
        delay(1)
        emit(1)
        delay(2)
        emit(2)
        throw TestException()
      }.ignoreElements(),
    )
  }

  @Test
  fun cancellation() = runTest {
    val flow = flowOf(1)
      .onEach { delay(1) }
      .repeat()
      .ignoreElements()

    var throwable: Throwable? = null
    val job = flow
      .onCompletion { throwable = it }
      .launchIn(this)

    delay(100)
    job.cancelAndJoin()

    assertIs<CancellationException>(
      assertNotNull(throwable),
    )
  }

  @Test
  fun withOtherOperators() = runTest {
    flowOf("a", "b", "c", "d")
      .onEach { delay(100) }
      .flatMapMerge { flowOf(it) }
      .ignoreElements()
      .flatMapMerge {
        @Suppress("UNREACHABLE_CODE") // just for test
        flowOf(it)
      }
      .test(listOf(Event.Complete))
  }
}
