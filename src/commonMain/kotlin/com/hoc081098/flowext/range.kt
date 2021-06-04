package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a [Flow] that emits a sequence of numbers within a specified range.
 * @param start The value of the first integer in the sequence.
 * @param count The number of sequential integers to generate.
 */
public fun range(start: Int, count: Int): Flow<Int> = flow {
  repeat(count) { emit(it + start) }
}
