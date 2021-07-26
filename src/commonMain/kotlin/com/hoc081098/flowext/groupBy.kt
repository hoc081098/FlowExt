package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Represents a Flow of values that have a common key.
 */
public interface GroupedFlow<K, T> : Flow<T> {
  /**
   * The key of this Flow.
   */
  public val key: K
}

@FlowPreview
public fun <T, K, V> Flow<T>.groupBy(
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
): Flow<GroupedFlow<K, V>> = GroupByFlow(this, keySelector, valueSelector)

@FlowPreview
public fun <T, K> Flow<T>.groupBy(
  keySelector: suspend (T) -> K,
): Flow<GroupedFlow<K, T>> = GroupByFlow(this, keySelector, { it })

@FlowPreview
internal class GroupedFlowImpl<K, T>(
  override val key: K,
  val channel: Channel<T>
) : GroupedFlow<K, T>, AbstractFlow<T>() {
  override suspend fun collectSafely(collector: FlowCollector<T>) {
    channel.consumeAsFlow().collect { collector.emit(it) }
  }
}

@FlowPreview
internal class GroupByFlow<T, K, V>(
  private val source: Flow<T>,
  private val keySelector: suspend (T) -> K,
  private val valueSelector: suspend (T) -> V,
) : AbstractFlow<GroupedFlow<K, V>>() {
  override suspend fun collectSafely(collector: FlowCollector<GroupedFlow<K, V>>) {
    val groups = hashMapOf<K, GroupedFlowImpl<K, V>>()

    try {
      source.collect { v ->
        val key = keySelector(v)
        val value = valueSelector(v)

        groups.getOrPut(key) {
          GroupedFlowImpl(key, Channel<V>(1)).also {
            collector.emit(it)
          }
        }.channel.send(value)
      }
    } catch (t: Throwable) {
      if (t is CancellationException) throw t
      groups.values.forEach { it.channel.close(t) }
      return
    }

    groups.values.forEach { it.channel.close() }
  }
}
