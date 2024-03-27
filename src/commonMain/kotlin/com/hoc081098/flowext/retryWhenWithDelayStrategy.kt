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

import kotlin.math.pow
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.retryWhen

/**
 * Interface that computes the delay between retries.
 */
public fun interface DelayStrategy {
  /**
   * Returns the [Duration] computed by this [DelayStrategy] to delay.
   * [Duration.ZERO] means passing without delay.
   *
   * @param cause
   * @param attempt starting from zero on the initial call
   */
  public fun nextDelay(cause: Throwable, attempt: Long): Duration

  /**
   * A delay strategy that doesn't introduce any delay between attempts.
   * Always returns [Duration.ZERO] for any cause and any attempt.
   */
  public object NoDelayStrategy : DelayStrategy {
    override fun nextDelay(cause: Throwable, attempt: Long): Duration = Duration.ZERO
  }

  /**
   * Fixed delay strategy, always returns constant delay for any cause and any attempt.
   */
  public class FixedTimeDelayStrategy(private val duration: Duration) : DelayStrategy {
    override fun nextDelay(cause: Throwable, attempt: Long): Duration = duration
  }

  /**
   * Delay strategy that increases the delay duration exponentially until a max duration has been reached.
   *
   * Implementation of backoff that increases the back off duration for each retry attempt.
   * When the duration has reached the max duration, it is no longer increased.
   *
   * Example: The [initialDelay] is 2000 ms, the [factor] is 1.5, and the [maxDelay] is 30000 ms.
   * For 10 attempts the sequence will be as follows:
   *
   * ```
   * -----------------------
   * | attempt# | back off |
   * |    1     |   2000   |
   * |    2     |   3000   |
   * |    3     |   4500   |
   * |    4     |   6750   |
   * |    5     |  10125   |
   * |    6     |  15187   |
   * |    7     |  22780   |
   * |    8     |  30000   |
   * |    9     |  30000   |
   * |    10    |  30000   |
   * -----------------------
   * ```
   */
  public class ExponentialBackoffDelayStrategy(
    private val initialDelay: Duration,
    private val factor: Double,
    private val maxDelay: Duration,
  ) : DelayStrategy {
    override fun nextDelay(cause: Throwable, attempt: Long): Duration =
      (if (attempt <= 0L) initialDelay else initialDelay * factor.pow(attempt.toDouble()))
        .coerceAtMost(maxDelay)
  }
}

/**
 * Retries collection of the given flow when an exception occurs in the upstream flow and the
 * [predicate] returns true. The predicate also receives an `attempt` number as parameter,
 * starting from zero on the initial call. When [predicate] returns true, the next retries will be
 * delayed after a duration computed by [DelayStrategy.nextDelay].
 *
 * This operator is *transparent* to exceptions that occur
 * in downstream flow and does not retry on exceptions that are thrown to cancel the flow.
 *
 * See [retryWhen] for more details.
 */
public fun <T> Flow<T>.retryWhenWithDelayStrategy(
  strategy: DelayStrategy,
  predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean,
): Flow<T> = retryWhen { cause, attempt ->
  predicate(cause, attempt).also {
    if (it) {
      delay(strategy.nextDelay(cause, attempt))
    }
  }
}

/**
 * Retries collection of the given flow with exponential backoff delay strategy
 * when an exception occurs in the upstream flow and the [predicate] returns true. When [predicate] returns true,
 * the next retries will be delayed after a duration computed by [DelayStrategy.ExponentialBackoffDelayStrategy].
 *
 * See [retryWhenWithDelayStrategy] and [DelayStrategy.ExponentialBackoffDelayStrategy] for more details.
 */
public fun <T> Flow<T>.retryWhenWithExponentialBackoff(
  initialDelay: Duration,
  factor: Double,
  maxDelay: Duration = Duration.INFINITE,
  predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean,
): Flow<T> = retryWhenWithDelayStrategy(
  strategy = DelayStrategy.ExponentialBackoffDelayStrategy(
    initialDelay = initialDelay,
    factor = factor,
    maxDelay = maxDelay,
  ),
  predicate = predicate,
)

/**
 * Retries collection of the given flow with exponential backoff delay strategy
 * when an exception occurs in the upstream flow and the [predicate] returns true. When [predicate] returns true,
 * the next retries will be delayed after a duration computed by [DelayStrategy.ExponentialBackoffDelayStrategy].
 *
 * See [retryWhenWithDelayStrategy] and [DelayStrategy.ExponentialBackoffDelayStrategy] for more details.
 */
public fun <T> Flow<T>.retryWithExponentialBackoff(
  initialDelay: Duration,
  factor: Double,
  maxAttempt: Long = Long.MAX_VALUE,
  maxDelay: Duration = Duration.INFINITE,
  predicate: suspend (cause: Throwable) -> Boolean = { true },
): Flow<T> {
  require(maxAttempt > 0) { "Expected positive amount of maxAttempt, but had $maxAttempt" }

  return retryWhenWithExponentialBackoff(
    initialDelay = initialDelay,
    factor = factor,
    maxDelay = maxDelay,
  ) { cause, attempt -> attempt < maxAttempt && predicate(cause) }
}
