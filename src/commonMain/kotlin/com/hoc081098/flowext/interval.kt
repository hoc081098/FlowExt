package com.hoc081098.flowext

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
public fun interval(
  initialDelay: Duration,
  delay: Duration,
): Flow<Long> {
  require(initialDelay >= Duration.ZERO) { "Expected non-negative delay, but has $initialDelay ms" }
  require(delay >= Duration.ZERO) { "Expected non-negative delay, but has $delay ms" }

  return flow {
    delay(initialDelay)

    var count = 0L
    while (true) {
      emit(count++)
      delay(delay)
    }
  }
}

public fun interval(
  initialDelayMillis: Long,
  delayMillis: Long,
): Flow<Long> {
  require(initialDelayMillis >= 0) { "Expected non-negative delay, but has $initialDelayMillis ms" }
  require(delayMillis >= 0) { "Expected non-negative delay, but has $delayMillis ms" }

  return flow {
    delay(initialDelayMillis)

    var count = 0L
    while (true) {
      emit(count++)
      delay(delayMillis)
    }
  }
}
