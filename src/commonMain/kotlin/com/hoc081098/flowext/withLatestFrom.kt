import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

private object NULL {
  @Suppress("UNCHECKED_CAST")
  inline fun <T> unbox(v: Any?): T = if (this === v) null as T else v as T
}

@ExperimentalCoroutinesApi
public fun <A, B, R> Flow<A>.withLatestFrom(
  other: Flow<B>,
  transform: suspend (A, B) -> R
): Flow<R> {
  return flow {
    coroutineScope {
      val otherValues = produce(capacity = Channel.CONFLATED) {
        other.collect {
          return@collect send(it ?: NULL)
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
            NULL.unbox(lastValue ?: return@collect)
          ),
        )
      }
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
@ExperimentalCoroutinesApi
public inline fun <A, B> Flow<A>.withLatestFrom(other: Flow<B>): Flow<Pair<A, B>> =
  withLatestFrom(other) { a, b -> a to b }
