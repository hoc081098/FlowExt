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

@file:Suppress("NOTHING_TO_INLINE")

package com.hoc081098.flowext

import kotlin.time.Duration
import kotlinx.coroutines.delay as coroutinesDelay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

// ----------------------------------------------- REPEAT FOREVER -----------------------------------------------

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely.
 */
public fun <T> Flow<T>.repeat(): Flow<T> =
  repeatInternal(flow = this, count = 0, infinite = true, delay = noDelay())

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a delay computed by [delay] function between each repetition.
 */
public fun <T> Flow<T>.repeat(delay: suspend (count: Int) -> Duration): Flow<T> =
  repeatInternal(flow = this, count = 0, infinite = true, delay = delaySelector(delay))

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a fixed [delay] between each repetition.
 */
public fun <T> Flow<T>.repeat(delay: Duration): Flow<T> =
  repeatInternal(flow = this, count = 0, infinite = true, delay = fixedDelay(delay))

// --------------------------------------------------- REPEAT COUNT ---------------------------------------------------

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times.
 */
public fun <T> Flow<T>.repeat(count: Int): Flow<T> =
  repeatInternal(flow = this, count = count, infinite = false, delay = noDelay())

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times,
 * with a delay computed by [delay] function between each repetition.
 */
public fun <T> Flow<T>.repeat(
  count: Int,
  delay: suspend (count: Int) -> Duration
): Flow<T> =
  repeatInternal(flow = this, count = count, infinite = false, delay = delaySelector(delay))

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a fixed [delay] between each repetition.
 */
public fun <T> Flow<T>.repeat(
  count: Int,
  delay: Duration
): Flow<T> = repeatInternal(flow = this, count = count, infinite = false, delay = fixedDelay(delay))

// ---------------------------------------------------- INTERNAL ----------------------------------------------------

private typealias DelayDurationSelector = suspend (count: Int) -> Duration

private inline fun noDelay(): DelayDurationSelector? = null
private inline fun fixedDelay(delay: Duration): DelayDurationSelector = { delay }
private inline fun delaySelector(noinline delay: DelayDurationSelector): DelayDurationSelector =
  delay

private fun <T> repeatInternal(
  flow: Flow<T>,
  count: Int,
  infinite: Boolean,
  delay: DelayDurationSelector?
): Flow<T> = when {
  infinite -> repeatIndefinitely(
    flow = flow,
    delay = delay
  )
  count <= 0 -> emptyFlow()
  else -> repeatAtMostCount(
    flow = flow,
    count = count,
    delay = delay
  )
}

private inline fun <T> repeatIndefinitely(
  flow: Flow<T>,
  noinline delay: DelayDurationSelector?
): Flow<T> {
  return when (delay) {
    null -> {
      flow {
        while (true) {
          emitAll(flow)
        }
      }
    }
    else -> {
      flow {
        var soFar = 1

        while (true) {
          emitAll(flow)
          coroutinesDelay(delay(soFar++))
        }
      }
    }
  }
}

private inline fun <T> repeatAtMostCount(
  flow: Flow<T>,
  count: Int,
  noinline delay: DelayDurationSelector?
): Flow<T> {
  return when (delay) {
    null -> {
      flow {
        repeat(count) {
          emitAll(flow)
        }
      }
    }
    else -> {
      flow {
        for (soFar in 1..count) {
          emitAll(flow)
          coroutinesDelay(delay(soFar))
        }
      }
    }
  }
}
