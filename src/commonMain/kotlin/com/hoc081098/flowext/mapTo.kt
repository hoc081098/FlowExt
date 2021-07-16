package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

public inline fun <T, R> Flow<T>.mapTo(value: R): Flow<R> =
  transform { return@transform emit(value) }
