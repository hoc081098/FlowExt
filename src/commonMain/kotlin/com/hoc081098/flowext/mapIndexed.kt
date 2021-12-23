package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow

/**
 * Returns a flow containing the results of applying the given [transform] function
 * to each value and its index in the original flow.
 */
public fun <T, R> Flow<T>.mapIndexed(transform: suspend (index: Int, value: T) -> R): Flow<R> =
  flow {
    collectIndexed { index, value ->
      return@collectIndexed emit(transform(index, value))
    }
  }
