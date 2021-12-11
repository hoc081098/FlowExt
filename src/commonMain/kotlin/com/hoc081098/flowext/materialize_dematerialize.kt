package com.hoc081098.flowext

import com.hoc081098.flowext.internal.ClosedException
import com.hoc081098.flowext.internal.checkOwnership
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * Represents all of the notifications from the source [Flow] as `value` emissions marked with their original types within [Event] objects.
 */
public fun <T> Flow<T>.materialize(): Flow<Event<T>> = map<T, Event<T>> { Event.Value(it) }
  .onCompletion { if (it === null) emit(Event.Complete) }
  .catch { emit(Event.Error(it)) }

/**
 * Converts a [Flow] of [Event] objects into the emissions that they represent.
 */
@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
public fun <T> Flow<Event<T>>.dematerialize(): Flow<T> = flow {
  try {
    collect {
      when (it) {
        Event.Complete -> throw ClosedException(this)
        is Event.Error -> throw it.error
        is Event.Value -> emit(it.value)
      }
    }
  } catch (e: ClosedException) {
    e.checkOwnership(this@flow)
  }
}
