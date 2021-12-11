package com.hoc081098.flowext

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.retryWhen
import kotlin.math.pow
import kotlin.time.Duration

public fun <T> Flow<T>.retryWhenWithDelayStrategy(
  strategy: DelayStrategy,
  predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean
): Flow<T> = retryWhen { cause, attempt ->
  predicate(cause, attempt).also {
    if (it) {
      delay(strategy.duration(cause, attempt))
    }
  }
}

public fun interface DelayStrategy {
  public fun duration(cause: Throwable, attempt: Long): Duration

  public object Immediate : DelayStrategy {
    override fun duration(cause: Throwable, attempt: Long): Duration = Duration.ZERO
  }

  public class Constant(private val duration: Duration) : DelayStrategy {
    override fun duration(cause: Throwable, attempt: Long): Duration = duration
  }

  public class Exponential(
    private val initialDelay: Duration,
    private val factor: Double,
    private val maxDelay: Duration,
  ) : DelayStrategy {
    override fun duration(cause: Throwable, attempt: Long): Duration =
      (if (attempt <= 0L) initialDelay else initialDelay * factor.pow(attempt.toDouble()))
        .coerceAtMost(maxDelay)
  }
}

public fun <T> Flow<T>.retryWhenWithExponentialBackoff(
  initialDelay: Duration,
  factor: Double,
  maxDelay: Duration = Duration.INFINITE,
  predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean
): Flow<T> = retryWhenWithDelayStrategy(
  strategy = DelayStrategy.Exponential(
    initialDelay = initialDelay,
    factor = factor,
    maxDelay = maxDelay,
  ),
  predicate = predicate,
)

public fun <T> Flow<T>.retryWithExponentialBackoff(
  initialDelay: Duration,
  factor: Double,
  maxAttempt: Long = Long.MAX_VALUE,
  maxDelay: Duration = Duration.INFINITE,
  predicate: suspend (cause: Throwable) -> Boolean = { true }
): Flow<T> {
  require(maxAttempt > 0) { "Expected positive amount of maxAttempt, but had $maxAttempt" }

  return retryWhenWithExponentialBackoff(
    initialDelay = initialDelay,
    factor = factor,
    maxDelay = maxDelay
  ) { cause, attempt -> attempt < maxAttempt && predicate(cause) }
}
