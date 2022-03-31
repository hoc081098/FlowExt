/*
 * MIT License
 *
 * Copyright (c) 2021 Petrus Nguyễn Thái Học
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

import com.hoc081098.flowext.internal.DONE_VALUE
import com.hoc081098.flowext.utils.NULL_VALUE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile

/**
 * Represents a Flow of values that have a common key.
 */
public interface GroupedFlow<K, T> : Flow<T> {
  /**
   * The key of this Flow.
   */
  public val key: K
}

@ExperimentalCoroutinesApi
public fun <T, K, V> Flow<T>.groupBy(
  channelBuilder: (key: K) -> MutableSharedFlow<V> = { MutableSharedFlow() },
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
): Flow<GroupedFlow<K, V>> = GroupByFlow(
  source = this,
  keySelector = keySelector,
  valueSelector = valueSelector,
  channelBuilder = channelBuilder
)

@ExperimentalCoroutinesApi
public fun <T, K> Flow<T>.groupBy(
  channelBuilder: (key: K) -> MutableSharedFlow<T> = { MutableSharedFlow() },
  keySelector: suspend (T) -> K,
): Flow<GroupedFlow<K, T>> = GroupByFlow(
  source = this,
  keySelector = keySelector,
  valueSelector = { it },
  channelBuilder = channelBuilder
)

internal class GroupedFlowImpl<K, T>(
  override val key: K,
  internal val channel: MutableSharedFlow<Any>,
) : GroupedFlow<K, T>, Flow<T> {
  override suspend fun collect(collector: FlowCollector<T>) =
    channel
      .takeWhile { it !== DONE_VALUE }
      .map { NULL_VALUE.unbox<T>(it) }
      .collect(collector)

  override fun toString() = "GroupedFlow(key=$key)"
}

@ExperimentalCoroutinesApi
internal class GroupByFlow<T, K, V>(
  private val source: Flow<T>,
  private val keySelector: suspend (T) -> K,
  private val valueSelector: suspend (T) -> V,
  private val channelBuilder: (key: K) -> MutableSharedFlow<V>,
) : Flow<GroupedFlow<K, V>> by (flow {
  val groups = hashMapOf<K, GroupedFlowImpl<K, V>>()

  try {
    source.collect { v ->
      val key = keySelector(v)
      val value = valueSelector(v)

      val group = groups[key]
      val channel = if (group === null) {
        val g = GroupedFlowImpl<K, V>(key, MutableSharedFlow(extraBufferCapacity = Channel.UNLIMITED))
        groups[key] = g
        println("[START EMIT] key=$key -> $g")
        emit(g)
        println("[DONE EMIT] key=$key -> $g")
        g.channel
      } else {
        group.channel
      }

      println("[START EMIT VALUE] key=$key -> value=$value flow=$channel")
      channel.emit(value ?: NULL_VALUE)
      println("[DONE EMIT VALUE] key=$key -> value=$value flow=$channel")
    }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Throwable) {

  } finally {
    println("Finally")
    groups.forEach { it.value.channel.emit(DONE_VALUE) }
    groups.clear()
  }
})