package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns a flow containing the results of applying the given [transform] function
 * to each value and its index in the original flow.
 */
public fun <T, R> Flow<T>.mapIndexed(transform: suspend (index: Int, value: T) -> R): Flow<R> =
  flow {
    var index = 0
    collect { value ->
      return@collect emit(
        transform(
          checkIndexOverflow(index++),
          value
        ),
      )
    }
  }

@Suppress("NOTHING_TO_INLINE")
internal inline fun checkIndexOverflow(index: Int): Int = if (index < 0) {
  throw ArithmeticException("Index overflow has happened.")
} else {
  index
}
