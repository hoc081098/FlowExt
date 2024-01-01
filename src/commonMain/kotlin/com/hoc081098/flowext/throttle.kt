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

import com.hoc081098.flowext.ThrottleConfiguration.LEADING
import com.hoc081098.flowext.ThrottleConfiguration.LEADING_AND_TRAILING
import com.hoc081098.flowext.ThrottleConfiguration.TRAILING
import com.hoc081098.flowext.internal.DONE_VALUE
import com.hoc081098.flowext.internal.INTERNAL_NULL_VALUE
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

/**
 * Define leading and trailing behavior.
 */
public enum class ThrottleConfiguration {
  /**
   * Emits only the first item in each window.
   */
  LEADING,

  /**
   * Emits only the last item in each window.
   */
  TRAILING,

  /**
   * Emits both the first item and the last item in each window.
   */
  LEADING_AND_TRAILING,
}

@Suppress("NOTHING_TO_INLINE")
private inline val ThrottleConfiguration.isLeading: Boolean
  get() = this === LEADING || this === LEADING_AND_TRAILING

private inline val ThrottleConfiguration.isTrailing: Boolean
  get() = this === TRAILING || this === LEADING_AND_TRAILING

/**
 * Returns a [Flow] that emits a value from the source [Flow], then ignores subsequent source values
 * for a [duration], then repeats this process for the next source value.
 *
 * * Example [ThrottleConfiguration.LEADING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500.milliseconds)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 4, 7, 10
 * ```
 *
 * * Example [ThrottleConfiguration.TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500.milliseconds, TRAILING)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 3, 6, 9, 10
 * ```
 *
 * * Example [ThrottleConfiguration.LEADING_AND_TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500.milliseconds, LEADING_AND_TRAILING)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 3, 4, 6, 7, 9, 10
 * ```
 */
@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttleTime(
  duration: Duration,
  throttleConfiguration: ThrottleConfiguration = LEADING,
): Flow<T> = throttleTime(throttleConfiguration) { duration }

/**
 * Returns a [Flow] that emits a value from the source [Flow], then ignores subsequent source values
 * for [timeMillis] milliseconds, then repeats this process for the next source value.
 *
 * * Example [ThrottleConfiguration.LEADING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 4, 7, 10
 * ```
 *
 * * Example [ThrottleConfiguration.TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500, TRAILING)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 3, 6, 9, 10
 * ```
 *
 * * Example [ThrottleConfiguration.LEADING_AND_TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(500, LEADING_AND_TRAILING)
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 3, 4, 6, 7, 9, 10
 * ```
 */
@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttleTime(
  timeMillis: Long,
  throttleConfiguration: ThrottleConfiguration = LEADING,
): Flow<T> = throttleTime(throttleConfiguration) { timeMillis.milliseconds }

/**
 * Returns a [Flow] that emits a value from the source [Flow], then ignores subsequent source values
 * for a duration determined by [durationSelector], then repeats this process for the next source value.
 *
 * * Example [ThrottleConfiguration.LEADING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime { 500.milliseconds }
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 4, 7, 10
 * ```
 *
 * * Example [ThrottleConfiguration.TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(TRAILING) { 500.milliseconds }
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 3, 6, 9, 10
 * ```
 *
 * * Example [ThrottleConfiguration.LEADING_AND_TRAILING]:
 *
 * ```kotlin
 * (1..10)
 *     .asFlow()
 *     .onEach { delay(200) }
 *     .throttleTime(LEADING_AND_TRAILING) { 500.milliseconds }
 * ```
 *
 * produces the following emissions
 *
 * ```text
 * 1, 3, 4, 6, 7, 9, 10
 * ```
 */
@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttleTime(
  throttleConfiguration: ThrottleConfiguration = LEADING,
  durationSelector: (value: T) -> Duration,
): Flow<T> = flow {
  val leading = throttleConfiguration.isLeading
  val trailing = throttleConfiguration.isTrailing
  val downstream = this

  coroutineScope {
    val scope = this

    // Produce the values using the default (rendezvous) channel
    val values = produce {
      collect { value -> send(value ?: INTERNAL_NULL_VALUE) }
    }

    var lastValue: Any? = null
    var throttled: Job? = null

    suspend fun trySend() {
      lastValue?.let { consumed ->
        check(lastValue !== DONE_VALUE)

        // Ensure we clear out our lastValue
        // before we emit, otherwise reentrant code can cause
        // issues here.
        lastValue = null // Consume the value
        return@let downstream.emit(INTERNAL_NULL_VALUE.unbox(consumed))
      }
    }

    val onWindowClosed = suspend {
      throttled = null

      if (trailing) {
        trySend()
      }
    }

    // Now consume the values until the original flow is complete.
    while (lastValue !== DONE_VALUE) {
      // wait for the next value
      select<Unit> {
        // When a throttling window ends, send the value if there is a pending value.
        throttled?.onJoin?.invoke(onWindowClosed)

        values.onReceiveCatching { result ->
          result
            .onSuccess { value ->
              lastValue = value

              // If we are not within a throttling window, immediately send the value (if leading is true)
              // and then start throttling.

              throttled?.let { return@onSuccess }
              if (leading) {
                trySend()
              }

              when (val duration = durationSelector(INTERNAL_NULL_VALUE.unbox(value))) {
                Duration.ZERO -> onWindowClosed()
                else -> throttled = scope.launch { delay(duration) }
              }
            }
            .onFailure {
              it?.let { throw it }

              // Once the original flow has completed, there may still be a pending value
              // waiting to be emitted. If so, wait for the throttling window to end and then
              // send it. That will complete this throttled flow.
              if (trailing && lastValue != null) {
                throttled?.run {
                  throttled = null
                  join()
                  trySend()
                }
              }

              lastValue = DONE_VALUE
            }
        }
      }
    }

    throttled?.run {
      throttled = null
      cancelAndJoin()
    }
  }
}
