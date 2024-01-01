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

import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns a [Flow] that emits a 0L after the [initialDelay] and ever-increasing numbers
 * after each [period] of time thereafter.
 *
 * @param initialDelay must be greater than or equal to [Duration.ZERO]
 * @param period must be greater than or equal to [Duration.ZERO]
 */
public fun interval(
  initialDelay: Duration,
  period: Duration,
): Flow<Long> {
  require(initialDelay >= Duration.ZERO) { "Expected non-negative delay, but has $initialDelay ms" }
  require(period >= Duration.ZERO) { "Expected non-negative period, but has $period ms" }

  return flow {
    delay(initialDelay)

    var count = 0L
    while (true) {
      emit(count++)
      delay(period)
    }
  }
}

/**
 * Returns a [Flow] that emits a 0L after the [initialDelayMillis] and ever-increasing numbers
 * after each [periodMillis] of time thereafter.
 *
 * @param initialDelayMillis must be non-negative
 * @param periodMillis must be non-negative
 */
public fun interval(
  initialDelayMillis: Long,
  periodMillis: Long,
): Flow<Long> {
  require(initialDelayMillis >= 0) { "Expected non-negative delay, but has $initialDelayMillis ms" }
  require(periodMillis >= 0) { "Expected non-negative periodMillis, but has $periodMillis ms" }

  return flow {
    delay(initialDelayMillis)

    var count = 0L
    while (true) {
      emit(count++)
      delay(periodMillis)
    }
  }
}
