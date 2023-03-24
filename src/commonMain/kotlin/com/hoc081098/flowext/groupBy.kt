/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hoc081098.flowext

import com.hoc081098.flowext.internal.AtomicBoolean
import com.hoc081098.flowext.internal.ClosedException
import com.hoc081098.flowext.internal.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Represents a Flow of values that have a common key.
 */
public interface GroupedFlow<K, T> : Flow<T> {
  /**
   * The key of this Flow.
   */
  public val key: K
}

internal val defaultChannel: (Any?) -> Channel<Any?> = { Channel(Channel.RENDEZVOUS) }

public fun <T, K, V> Flow<T>.groupBy(
  channelBuilder: (key: K) -> Channel<V> = defaultChannel as (Any?) -> Channel<V>,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V
): Flow<GroupedFlow<K, V>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = valueSelector,
  channelBuilder = channelBuilder
)

public fun <T, K> Flow<T>.groupBy(
  channelBuilder: (key: K) -> Channel<T> = defaultChannel as (Any?) -> Channel<T>,
  keySelector: suspend (T) -> K
): Flow<GroupedFlow<K, T>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = { it },
  channelBuilder = channelBuilder
)

private class GroupedFlowImpl<K, V>(
  override val key: K,
  private val channel: Channel<V>,
  private val onCancel: () -> Unit
) : GroupedFlow<K, V>, Flow<V> {
  internal suspend fun send(element: V) = channel.send(element)

  internal fun close(cause: Throwable?) = channel.close(cause)

  override suspend fun collect(collector: FlowCollector<V>) {
    try {
      channel.consumeEach { collector.emit(it) }
    } catch (e: CancellationException) {
      onCancel()
      println("Group cancel $this $e")
      throw e
    }
  }

  override fun toString() = "${super.toString()}(key=$key, channel=$channel)"
}

private fun <T, K, V> groupByInternal(
  source: Flow<T>,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
  channelBuilder: (key: K) -> Channel<V>
): Flow<GroupedFlow<K, V>> = flow {
  val collector = this

  val groups = ConcurrentHashMap<K, GroupedFlowImpl<K, V>>()
  val cancelled = AtomicBoolean()

  try {
    source.collect { value ->
      val key = keySelector(value)
      val g = groups[key]

      println("key=$key")
      println("cancelled=${cancelled.value}")
      println("groups=$groups")
      println("g=$g")

      if (g !== null) {
        emitToGroup(valueSelector, value, g)
      } else {
        if (cancelled.value) {
          // if the main has been cancelled, stop creating groups
          // and skip this value
          if (groups.isEmpty()) {
            throw ClosedException(collector)
          }
        } else {
          val group = GroupedFlowImpl(
            key = key,
            channel = channelBuilder(key),
            onCancel = {
              groups.remove(key)
              if (groups.isEmpty() && cancelled.value) {
                throw ClosedException(collector)
              }
            }
          )
          groups[key] = group

          try {
            collector.emit(group)
          } catch (e: CancellationException) {
            // cancelling the main source means we don't want any more groups
            // but running groups still require new values
            cancelled.value = true

            // we only kill our subscription to the source if we have
            // no active groups. As stated above, consider this scenario:
            // source.groupBy(fn).take(2).
            if (groups.isEmpty()) {
              throw ClosedException(collector)
            }
          }

          emitToGroup(valueSelector, value, group)
        }
      }
    }

    groups.forEach { it.value.close(null) }
  } catch (e: ClosedException) {
    if (e.owner === collector) {
      groups.forEach { it.value.close(null) }
    } else {
      groups.forEach { it.value.close(e) }

      throw e
    }
  } catch (e: Throwable) {
    groups.forEach { it.value.close(e) }

    throw e
  } finally {
    groups.clear()
  }
}

private suspend inline fun <K, T, V> emitToGroup(
  crossinline valueSelector: suspend (T) -> V,
  value: T,
  group: GroupedFlowImpl<K, V>
) {
  group.send(valueSelector(value))
  println("emitToGroup $group")
}
