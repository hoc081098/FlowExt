/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
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

import com.hoc081098.flowext.utils.BaseStepTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class NeverFlowTest : BaseStepTest() {
  @Test
  fun testNeverFlowSameInstance() {
    assertSame(neverFlow(), neverFlow())
    assertSame(NeverFlow, neverFlow())
    assertSame(NeverFlow, NeverFlow.Companion)
  }

  @Test
  fun testNeverFlow() = runTest(dispatchTimeout = 2.5.seconds) {
    val list = mutableListOf<Any?>()
    val job = launch { neverFlow().toList(list) }
    val intervalJob = interval(ZERO, 100.milliseconds).launchIn(this)

    runCurrent()

    advanceTimeBy(500)
    runCurrent()
    assertTrue(list.isEmpty())

    withContext(Dispatchers.Default) {
      val d = 2.seconds / 10

      repeat(10) {
        runCurrent()
        delay(d)
      }
    }

    advanceTimeBy(500)
    runCurrent()
    assertTrue(list.isEmpty())

    job.cancelAndJoin()
    intervalJob.cancelAndJoin()
  }
}
