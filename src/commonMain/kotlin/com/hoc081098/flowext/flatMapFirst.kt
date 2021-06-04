package com.hoc081098.flowext

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.flatMapFirst(transform: suspend (value: T) -> Flow<R>): Flow<R> =
    map(transform).flattenFirst()

@ExperimentalCoroutinesApi
public fun <T> Flow<Flow<T>>.flattenFirst(): Flow<T> = channelFlow {
    val outerScope = this
    val busy = AtomicBoolean(false)

    collect { inner ->
        if (busy.compareAndSet(expect = false, update = true)) {
            // Do not pay for dispatch here, it's never necessary
            launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    inner.collect { outerScope.send(it) }
                    busy.value = false
                } catch (e: CancellationException) {
                    // cancel outer scope on cancellation exception, too
                    outerScope.cancel(e)
                }
            }
        }
    }
}