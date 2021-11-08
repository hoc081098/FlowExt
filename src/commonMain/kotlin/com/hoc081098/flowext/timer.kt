package com.hoc081098.flowext

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Creates a [Flow] that will wait for a specified time, before emitting the [value].
 */
public fun <T> timer(value: T, timeMillis: Long): Flow<T> = flow {
  delay(timeMillis)
  emit(value)
}

/**
 * Creates a [Flow] that will wait for a given [duration], before emitting the [value].
 */
@ExperimentalTime
public fun <T> timer(value: T, duration: Duration): Flow<T> = flow {
  delay(duration)
  emit(value)
}
