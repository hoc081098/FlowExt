package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * Emits the values emitted by the source [Flow] until a [notifier] [Flow] emits a value or completes.
 *
 * @param notifier The [Flow] whose first emitted value or complete event
 * will cause the output [Flow] of [takeUntil] to stop emitting values from the source [Flow].
 */
@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.takeUntil(notifier: Flow<R>): Flow<T> = flow {
  try {
    coroutineScope {
      val job = launch(start = CoroutineStart.UNDISPATCHED) {
        notifier.take(1).collect()
        throw ClosedException(this@flow)
      }

      collect { emit(it) }
      job.cancel()
    }
  } catch (e: ClosedException) {
    if (e.owner !== this) throw e
  }
}
