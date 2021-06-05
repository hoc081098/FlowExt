package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.takeUntil(notifier: Flow<R>): Flow<T> = flow {
  coroutineScope {
    launch {
      notifier.take(1).collect()
      throw ClosedException
    }

    collect { emit(it) }
    throw ClosedException
  }
}.catch {
  if (it !is ClosedException) throw it
}

private object ClosedException : Exception()
