/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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

import com.hoc081098.flowext.utils.NULL_VALUE
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

// ----------------------------------------------- REPEAT FOREVER -----------------------------------------------

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely.
 */
public fun <T> Flow<T>.repeat(): Flow<T> = repeatInternal(flow = this, count = 0, infinite = true, delayDuration = null)

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a delay computed by [delayDuration] function between each repetition.
 */
public fun <T> Flow<T>.repeat(delayDuration: (count: Int) -> Duration): Flow<T> =
  repeatInternal(flow = this, count = 0, infinite = true, delayDuration = delayDuration)

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a delay between each repetition.
 */
public fun <T> Flow<T>.repeat(delayDuration: Duration): Flow<T> =
  repeatInternal(flow = this, count = 0, infinite = true) { delayDuration }

// --------------------------------------------------- REPEAT COUNT ---------------------------------------------------

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times.
 */
public fun <T> Flow<T>.repeat(count: Int): Flow<T> =
  repeatInternal(flow = this, count = count, infinite = false, delayDuration = null)

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times,
 * with a delay computed by [delayDuration] function between each repetition.
 */
public fun <T> Flow<T>.repeat(
  count: Int,
  delayDuration: (count: Int) -> Duration
): Flow<T> = repeatInternal(flow = this, count = count, infinite = false, delayDuration = delayDuration)

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a delay between each repetition.
 */
public fun <T> Flow<T>.repeat(
  count: Int,
  delayDuration: Duration
): Flow<T> = repeatInternal(flow = this, count = count, infinite = false) { delayDuration }

// ---------------------------------------------------- INTERNAL ----------------------------------------------------

private fun <T> repeatInternal(
  flow: Flow<T>,
  count: Int,
  infinite: Boolean,
  delayDuration: ((count: Int) -> Duration)?
): Flow<T> = when {
  infinite -> repeatIndefinitely(
    flow = flow,
    delayDurationOrNullValue = delayDuration ?: NULL_VALUE
  )
  count <= 0 -> emptyFlow()
  else -> repeatAtMostCount(
    flow = flow,
    count = count,
    delayDurationOrNullValue = delayDuration ?: NULL_VALUE
  )
}

private inline fun <T> repeatIndefinitely(
  flow: Flow<T>,
  delayDurationOrNullValue: Any // NULL_VALUE | (Int) -> Duration
): Flow<T> {
  val delayDuration = NULL_VALUE.unbox<((Int) -> Duration)?>(delayDurationOrNullValue)

  return if (delayDuration === null) {
    flow {
      while (true) {
        emitAll(flow)
      }
    }
  } else {
    flow {
      var soFar = 1

      while (true) {
        emitAll(flow)
        delay(delayDuration(soFar++))
      }
    }
  }
}

private inline fun <T> repeatAtMostCount(
  flow: Flow<T>,
  count: Int,
  delayDurationOrNullValue: Any // NULL_VALUE | (Int) -> Duration
): Flow<T> {
  val delayDuration = NULL_VALUE.unbox<((Int) -> Duration)?>(delayDurationOrNullValue)

  return if (delayDuration === null) {
    flow {
      repeat(count) {
        emitAll(flow)
      }
    }
  } else {
    flow {
      for (soFar in 1..count) {
        emitAll(flow)
        delay(delayDuration(soFar))
      }
    }
  }
}
