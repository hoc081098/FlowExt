package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

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
  internal val channel: Channel<T>
) : GroupedFlow<K, T>, AbstractFlow<T>() {
  private val flow = channel.consumeAsFlow()

  override suspend fun collectSafely(collector: FlowCollector<T>) =
    flow.collect { collector.emit(it) }

  override fun toString() = "GroupedFlow(key=$key)"
}

@ExperimentalCoroutinesApi
@FlowPreview
internal class GroupByFlow<T, K, V>(
  private val source: Flow<T>,
  private val keySelector: suspend (T) -> K,
  private val valueSelector: suspend (T) -> V,
) : AbstractFlow<GroupedFlow<K, V>>() {
  override suspend fun collectSafely(collector: FlowCollector<GroupedFlow<K, V>>) {
    val groups = hashMapOf<K, GroupedFlowImpl<K, V>>()
    var throwable = null as Throwable?

    try {
      source.collect { v ->
        val key = keySelector(v)
        val value = valueSelector(v)

        val group = groups[key]
        val channel = if (group === null) {
          val g = GroupedFlowImpl<K, V>(key, Channel())
          groups[key] = g
          collector.emit(g)
          g.channel
        } else {
          group.channel
        }

        println("$key -> $channel ${channel.isEmpty} ${channel.isClosedForReceive} ${channel.isClosedForSend}")
        channel.send(value)
        println("send $key -> $value\n")
      }
    } catch (t: Throwable) {
      throwable = t
    } finally {
      println("Finally $throwable")

      if (throwable is CancellationException) {
        groups.clear()
        throw throwable
      } else {
        groups.values.forEach { it.channel.close(throwable) }
        groups.clear()
      }
    }
  }
}
