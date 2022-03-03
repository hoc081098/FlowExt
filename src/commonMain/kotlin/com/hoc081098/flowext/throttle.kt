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

import com.hoc081098.flowext.ThrottleConfiguration.LEADING
import com.hoc081098.flowext.ThrottleConfiguration.LEADING_AND_TRAILING
import com.hoc081098.flowext.ThrottleConfiguration.TRAILING
import com.hoc081098.flowext.utils.NULL_VALUE
import com.hoc081098.flowext.utils.Symbol
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.native.concurrent.SharedImmutable
import kotlin.time.Duration

/*
 * Symbol used to indicate that the flow is complete.
 * It should never leak to the outside world.
 */
@SharedImmutable
private val DONE_VALUE = Symbol("DONE_VALUE")

public enum class ThrottleConfiguration {
  LEADING,
  TRAILING,
  LEADING_AND_TRAILING,
}

@Suppress("NOTHING_TO_INLINE")
private inline val ThrottleConfiguration.destructured: Pair<Boolean, Boolean>
  get() = (this === LEADING || this === LEADING_AND_TRAILING) to (this === TRAILING || this === LEADING_AND_TRAILING)

@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttleTime(
  duration: Duration,
  throttleConfiguration: ThrottleConfiguration = LEADING,
): Flow<T> = throttle(throttleConfiguration) { timer(Unit, duration) }

@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttleTime(
  timeMillis: Long,
  throttleConfiguration: ThrottleConfiguration = LEADING,
): Flow<T> = throttle(throttleConfiguration) { timer(Unit, timeMillis) }

@ExperimentalCoroutinesApi
public fun <T> Flow<T>.throttle(
  throttleConfiguration: ThrottleConfiguration = LEADING,
  durationSelector: (value: T) -> Flow<Unit>,
): Flow<T> = flow {
  val (leading, trailing) = throttleConfiguration.destructured
  val downstream = this

  coroutineScope {
    val scope = this

    // Produce the values using the default (rendezvous) channel
    val values = produce {
      collect { value -> send(value ?: NULL_VALUE) }
    }

    var lastValue: Any? = null
    var throttled: Job? = null

    suspend fun trySend() {
      println("    >>> TrySend $lastValue")
      check(lastValue == null || lastValue != DONE_VALUE)

      lastValue?.let { consumed ->
        // Ensure we clear out our lastValue
        // before we emit, otherwise reentrant code can cause
        // issues here.
        lastValue = null // Consume the value
        downstream.emit(NULL_VALUE.unbox(consumed))
      }
    }

    suspend fun cleanupThrottling() {
      throttled?.run {
        throttled = null
        cancelAndJoin()
      }
    }

    // Now consume the values
    while (lastValue !== DONE_VALUE) {
      println("*** while: lastValue=$lastValue throttled=$throttled")

      // wait for the next value
      select<Unit> {
        values.onReceiveCatching { result ->
          result
            .onSuccess { value ->
              println("    <<< while: onSuccess value=$value")

              lastValue = value

              // If we are not within a throttling window, immediately send the value (if leading is true)
              // and then start throttling.

              throttled?.let { return@onSuccess }
              if (leading) {
                trySend()
              }
              throttled = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                durationSelector(NULL_VALUE.unbox(value))
                  .take(1)
                  .collect()
              }
            }
            .onFailure {
              println("    <<< while: onFailure throwable=$it")

              it?.let { throw it }

              cleanupThrottling()
              lastValue = DONE_VALUE
            }
        }

        // When a throttling window ends, send the value if there is a pending value.
        (throttled ?: return@select).onJoin {
          println("    <<< while: onJoin")
          throttled = null

          if (trailing) {
            trySend()
          }
        }
      }

      println("<<< done select: lastValue=$lastValue, throttled=$throttled\n")
    }

    cleanupThrottling()
  }
}
