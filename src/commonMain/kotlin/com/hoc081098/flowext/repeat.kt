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
 *
 * Note: If the source [Flow] is completed synchronously immediately (e.g. [emptyFlow]),
 * this will cause an infinite loop.
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(): Flow<T> =
  repeatInternal(
    flow = this,
    count = 0,
    infinite = true,
    delay = noDelay(),
  )

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a delay computed by [delay] function between each repetition.
 *
 * Note: If the source [Flow] is completed synchronously immediately (e.g. [emptyFlow]),
 * and [delay] returns [Duration.ZERO] or a negative value,
 * this will cause an infinite loop.
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(delay: suspend (count: Int) -> Duration): Flow<T> =
  repeatInternal(
    flow = this,
    count = 0,
    infinite = true,
    delay = delaySelector(delay),
  )

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a fixed [delay] between each repetition.
 *
 * Note: If the source [Flow] is completed synchronously immediately (e.g. [emptyFlow]),
 * and [delay] is [Duration.ZERO], this will cause an infinite loop.
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(delay: Duration): Flow<T> =
  repeatInternal(
    flow = this,
    count = 0,
    infinite = true,
    delay = fixedDelay(delay),
  )

// --------------------------------------------------- REPEAT COUNT ---------------------------------------------------

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times.
 * If [count] is zero or negative, the resulting [Flow] completes immediately without emitting any items (i.e. [emptyFlow]).
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(count: Int): Flow<T> =
  repeatInternal(
    flow = this,
    count = count,
    infinite = false,
    delay = noDelay(),
  )

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] at most [count] times,
 * with a delay computed by [delay] function between each repetition.
 *
 * If [count] is zero or negative, the resulting [Flow] completes immediately without emitting any items (i.e. [emptyFlow]).
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(
  count: Int,
  delay: suspend (count: Int) -> Duration,
): Flow<T> =
  repeatInternal(
    flow = this,
    count = count,
    infinite = false,
    delay = delaySelector(delay),
  )

/**
 * Returns a [Flow] that repeats all values emitted by the original [Flow] indefinitely,
 * with a fixed [delay] between each repetition.
 *
 * If [count] is zero or negative, the resulting [Flow] completes immediately without emitting any items (i.e. [emptyFlow]).
 */
@FlowExtPreview
public fun <T> Flow<T>.repeat(
  count: Int,
  delay: Duration,
): Flow<T> =
  repeatInternal(
    flow = this,
    count = count,
    infinite = false,
    delay = fixedDelay(delay),
  )

// ---------------------------------------------------- INTERNAL ----------------------------------------------------

private typealias DelayDurationSelector = suspend (count: Int) -> Duration

private inline fun noDelay(): DelayDurationSelector? = null

private inline fun fixedDelay(delay: Duration): DelayDurationSelector? =
  if (delay.isZeroOrNegative()) {
    noDelay()
  } else {
    FixedDelayDurationSelector(delay)
  }

private inline fun delaySelector(noinline delay: DelayDurationSelector): DelayDurationSelector =
  delay

private inline fun Duration.isZeroOrNegative() = this == Duration.ZERO || isNegative()

/**
 * Used when the delay duration is fixed.
 * This is an optimization to avoid integer overflow.
 */
private class FixedDelayDurationSelector(val duration: Duration) : DelayDurationSelector {
  override suspend fun invoke(count: Int): Duration = duration
}

@FlowExtPreview
private fun <T> repeatInternal(
  flow: Flow<T>,
  count: Int,
  infinite: Boolean,
  delay: DelayDurationSelector?,
): Flow<T> = when {
  infinite -> repeatIndefinitely(
    flow = flow,
    delay = delay,
  )
  count <= 0 -> emptyFlow()
  else -> repeatAtMostCount(
    flow = flow,
    count = count,
    delay = delay,
  )
}

@FlowExtPreview
private fun <T> repeatIndefinitely(
  flow: Flow<T>,
  delay: DelayDurationSelector?,
): Flow<T> = when (delay) {
  null -> flow {
    while (true) {
      emitAll(flow)
    }
  }
  is FixedDelayDurationSelector -> flow {
    while (true) {
      emitAll(flow)
      coroutinesDelay(delay.duration)
    }
  }
  else -> flow {
    var soFar = 1

    while (true) {
      emitAll(flow)
      coroutinesDelay(delay(soFar++))
    }
  }
}

@FlowExtPreview
private fun <T> repeatAtMostCount(
  flow: Flow<T>,
  count: Int,
  delay: DelayDurationSelector?,
): Flow<T> = when (delay) {
  null -> flow {
    repeat(count) {
      emitAll(flow)
    }
  }
  is FixedDelayDurationSelector -> flow {
    repeat(count) {
      emitAll(flow)
      coroutinesDelay(delay.duration)
    }
  }
  else -> flow {
    for (soFar in 1..count) {
      emitAll(flow)
      coroutinesDelay(delay(soFar))
    }
  }
}
