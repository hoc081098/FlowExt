package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

public fun <T> Flow<T>.materialize(): Flow<Event<T>> = map<T, Event<T>> { Event.Value(it) }
  .onCompletion { if (it === null) emit(Event.Complete) }
  .catch { emit(Event.Error(it)) }

public fun <T> Flow<Event<T>>.dematerialize(): Flow<T> = flow {
  try {
    collect {
      when (it) {
        Event.Complete -> throw ClosedException(this)
        is Event.Error -> throw it.throwable
        is Event.Value -> emit(it.value)
      }
    }
  } catch (e: ClosedException) {
    if (e.owner !== this) throw e
  }
}
