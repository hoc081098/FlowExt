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
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class RetryWhenWithDelayStrategyTest : BaseTest() {
  @Test
  fun test() = runTest {
    var count = 0
    val retries = 3
    val flow = flow {
      emit(1)
      throw TestException((count++).toString())
    }

    val sum =
      flow.retryWhenWithDelayStrategy(DelayStrategy.FixedTimeDelayStrategy(100.milliseconds)) { cause, attempt ->
        assertEquals(
          attempt.toString(),
          assertIs<TestException>(cause).message,
        )
        attempt < retries
      }.catch { cause ->
        assertEquals(
          retries.toString(),
          assertIs<TestException>(cause).message,
        )
      }.fold(0, Int::plus)
    assertEquals(4, sum)
  }
}

class DelayStrategyTest {
  private val cause = TestException()
  private val attempt = 0L

  @Test
  fun testImmediate() {
    repeat(10) {
      assertEquals(
        Duration.ZERO,
        DelayStrategy.NoDelayStrategy.nextDelay(cause, attempt),
      )
    }
  }

  @Test
  fun testConstant() {
    val duration = 100.milliseconds

    repeat(10) {
      assertEquals(
        duration,
        DelayStrategy.FixedTimeDelayStrategy(duration).nextDelay(cause, attempt),
      )
    }
  }

  @Test
  fun testExponential() {
    fun every(initialDelay: Duration, factor: Double, maxDelay: Duration) {
      val strategy = DelayStrategy.ExponentialBackoffDelayStrategy(
        initialDelay = initialDelay,
        factor = factor,
        maxDelay = maxDelay,
      )

      fun Duration.coerce(): Duration = coerceAtMost(maxDelay)

      assertEquals(
        initialDelay.coerce(),
        strategy.nextDelay(cause, 0),
      )
      assertEquals(
        (initialDelay * factor).coerce(),
        strategy.nextDelay(cause, 1),
      )
      assertEquals(
        (initialDelay * factor * factor).coerce(),
        strategy.nextDelay(cause, 2),
      )
      assertEquals(
        (initialDelay * factor * factor * factor).coerce(),
        strategy.nextDelay(cause, 3),
      )
      assertEquals(
        (initialDelay * factor * factor * factor * factor).coerce(),
        strategy.nextDelay(cause, 4),
      )
      assertEquals(
        (initialDelay * factor * factor * factor * factor * factor).coerce(),
        strategy.nextDelay(cause, 5),
      )
      assertEquals(
        (initialDelay * factor.pow(100)).coerce(),
        strategy.nextDelay(cause, 100),
      )
    }

    listOf<Triple<Duration, Double, Duration>>(
      Triple(100.milliseconds, 1.0, 2_000.milliseconds),
      Triple(100.milliseconds, 1.5, 2_000.milliseconds),
      Triple(100.milliseconds, 2.0, 2_000.milliseconds),
      Triple(100.milliseconds, 2.7, 2_000.milliseconds),
      Triple(100.milliseconds, 3.69, 2_000.milliseconds),
      Triple(100.milliseconds, 100.0, 2_000.milliseconds),
      //
      Triple(10.milliseconds, 2.0, 1_000.milliseconds),
      Triple(20.milliseconds, 2.0, 1_000.milliseconds),
      Triple(30.milliseconds, 2.0, 1_000.milliseconds),
      Triple(40.milliseconds, 2.0, 1_000.milliseconds),
      Triple(50.milliseconds, 2.0, 1_000.milliseconds),
      Triple(23823.milliseconds, 2.0, 1_000.milliseconds),
      //
      Triple(100.milliseconds, 1.5, 1_000.milliseconds),
      Triple(100.milliseconds, 1.5, 2_000.milliseconds),
      Triple(100.milliseconds, 1.5, 3_000.milliseconds),
      Triple(100.milliseconds, 1.5, 4_000.milliseconds),
      Triple(100.milliseconds, 1.5, 5_000.milliseconds),
      Triple(100.milliseconds, 1.5, 6_000.milliseconds),
    ).forEach { (a, b, c) -> every(a, b, c) }
  }
}
