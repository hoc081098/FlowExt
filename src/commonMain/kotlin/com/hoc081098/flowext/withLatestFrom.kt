package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Merges two [Flow]s into one [Flow] by combining each value from self with the latest value from the second [Flow], if any.
 * Values emitted by self before the second [Flow] has emitted any values will be omitted.
 *
 * @param other Second [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest value from the second [Flow], if any.
 */
@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
public fun <A, B, R> Flow<A>.withLatestFrom(
  other: Flow<B>,
  transform: suspend (A, B) -> R
): Flow<R> {
  return flow {
    coroutineScope {
      val otherValues = Channel<Any>(Channel.CONFLATED)
      launch(start = CoroutineStart.UNDISPATCHED) {
        other.collect {
          return@collect otherValues.send(it ?: NULL_VALUE)
        }
      }

      var lastValue: Any? = null
      collect { value ->
        otherValues
          .tryReceive()
          .onSuccess { lastValue = it }

        emit(
          transform(
            value,
            NULL_VALUE.unbox(lastValue ?: return@collect)
          ),
        )
      }
    }
  }
}

@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
@Suppress("NOTHING_TO_INLINE")
public inline fun <A, B> Flow<A>.withLatestFrom(other: Flow<B>): Flow<Pair<A, B>> =
  withLatestFrom(other) { a, b -> a to b }
