package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

/**
 * Emits the given constant value on the output Flow every time the source Flow emits a value.
 *
 * @param value The value to map each source value to.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <T, R> Flow<T>.mapTo(value: R): Flow<R> =
  transform { return@transform emit(value) }
