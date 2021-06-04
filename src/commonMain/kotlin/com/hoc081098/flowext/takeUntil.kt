package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.takeUntil(notifier: Flow<R>): Flow<T> = channelFlow {
    val outerScope = this

    launch {
        try {
            notifier.take(1).collect()
            close()
        } catch (e: CancellationException) {
            outerScope.cancel(e) // cancel outer scope on cancellation exception, too
        }
    }

    launch {
        try {
            collect { send(it) }
            close()
        } catch (e: CancellationException) {
            outerScope.cancel(e) // cancel outer scope on cancellation exception, too
        }
    }
}
