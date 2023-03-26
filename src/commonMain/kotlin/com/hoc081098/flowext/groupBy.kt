/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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
import com.hoc081098.flowext.internal.DONE_VALUE
import com.hoc081098.flowext.internal.identitySuspendFunction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Represents a Flow of values that have a common key.
 *
 * Note: A [GroupedFlow] will cache the items it is to emit until such time as it is subscribed to.
 * For this reason, in order to avoid memory leaks, you should not simply ignore those
 * [GroupedFlow]s that do not concern you. Instead, you can signal to them that they may discard
 * their buffers by applying an operator like `take(0)` to them.
 */
public interface GroupedFlow<K, T> : Flow<T> {
  /**
   * The key that identifies the group of items emitted by this [GroupedFlow].
   */
  public val key: K
}

/**
 * Groups the items emitted by the current [Flow] according to a specified criterion,
 * and emits these grouped items as [GroupedFlow]s.
 *
 * The emitted [GroupedFlow] allows only a single [FlowCollector] during its lifetime
 * and if this [FlowCollector] cancels before the source terminates,
 * the next emission by the source having the same key will trigger a new [GroupedFlow] emission.
 *
 * If the upstream throw an exception, the returned [Flow] and all active inner [GroupedFlow]s
 * will signal the same exception.
 *
 * @param keySelector a function that extracts the key for each item
 * @param valueSelector a function that extracts the return value for each item
 * @see GroupedFlow
 */
@FlowExtPreview
@ExperimentalCoroutinesApi
public fun <T, K, V> Flow<T>.groupBy(
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V
): Flow<GroupedFlow<K, V>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = valueSelector
)

/**
 * Groups the items emitted by the current [Flow] according to a specified criterion,
 * and emits these grouped items as [GroupedFlow]s.
 *
 * The emitted [GroupedFlow] allows only a single [FlowCollector] during its lifetime
 * and if this [FlowCollector] cancels before the source terminates,
 * the next emission by the source having the same key will trigger a new [GroupedFlow] emission.
 *
 * If the upstream throw an exception, the returned [Flow] and all active inner [GroupedFlow]s
 * will signal the same exception.
 *
 * @param keySelector a function that extracts the key for each item
 * @see GroupedFlow
 */
@Suppress("UNCHECKED_CAST")
@FlowExtPreview
@ExperimentalCoroutinesApi
public fun <T, K> Flow<T>.groupBy(
  keySelector: suspend (T) -> K
): Flow<GroupedFlow<K, T>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = identitySuspendFunction as (suspend (T) -> T)
)

@ExperimentalCoroutinesApi
private class GroupedFlowImpl<K, V>(
  override val key: K,
  private val channel: Channel<Any?>,
  private val onCancelHandler: () -> Unit
) : GroupedFlow<K, V>, Flow<V> {
  private val calledOnCloseHandler = AtomicBoolean()
  private val completed = CompletableDeferred<Unit>()

  init {
    channel.invokeOnClose {
      // only call onCloseHandler when the channel is cancelled.
      if (calledOnCloseHandler.compareAndSet(expect = false, update = true)) {
        onCancelHandler()
      }
    }
  }

  suspend fun send(element: V) = channel.send(element)

  suspend fun close(cause: Throwable?) {
    if (cause == null) {
      channel.send(DONE_VALUE)
    } else {
      channel.send(GroupError(cause))
    }

    // set calledOnCloseHandler to true to prevent calling onCloseHandler when we close the channel.
    calledOnCloseHandler.value = true
    channel.close()

    completed.await()
  }

  override suspend fun collect(collector: FlowCollector<V>) {
    channel.consumeEach { value ->
      when {
        value === DONE_VALUE -> {
          completed.complete(Unit)
          return
        }
        value is GroupError -> {
          completed.complete(Unit)
          throw value.cause
        }
        else -> @Suppress("UNCHECKED_CAST") collector.emit(value as V)
      }
    }
  }

  override fun toString() = "${super.toString()}(key=$key, channel=$channel)"
}

@ExperimentalCoroutinesApi
private suspend inline fun <K, T, V> emitToGroup(
  crossinline valueSelector: suspend (T) -> V,
  value: T,
  group: GroupedFlowImpl<K, V>
) = group.send(valueSelector(value))

private class GroupCancellation(val key: Any?) {
  override fun toString(): String = "GroupCancellation(key=$key)"
}

private class GroupError(val cause: Throwable) {
  override fun toString(): String = "GroupError(cause=$cause)"
}

@FlowExtPreview
@ExperimentalCoroutinesApi
private fun <T, K, V> groupByInternal(
  source: Flow<T>,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
  innerBufferSize: Int = Channel.RENDEZVOUS // TODO: Consider making this configurable
): Flow<GroupedFlow<K, V>> = flow {
  val collector = this

  val groups = linkedMapOf<K, GroupedFlowImpl<K, V>>()
  val groupCancellationChannel = Channel<GroupCancellation>(Channel.UNLIMITED)

  try {
    coroutineScope {
      // Produce the values using the default (rendezvous) channel
      val values = produce {
        source.collect { send(it) }
      }

      var done = false
      var cancelled = false

      while (!done) {
        // receive groupCancellationChannel until it is empty or channel is closed or faied.
        while (true) {
          val groupCancellationChannelResult = groupCancellationChannel
            .tryReceive()
            .onSuccess {
              @Suppress("UNCHECKED_CAST")
              val key = it.key as K
              // remove the group from the map
              // and check if we have no more groups and the main has been cancelled
              // to stop the loop.
              groups.remove(key)
              if (groups.isEmpty() && cancelled) {
                done = true
              }
            }

          if (!groupCancellationChannelResult.isSuccess) {
            break
          }
        }

        // now, we can receive from the values channel
        values
          .receiveCatching()
          .onSuccess {
            val key = keySelector(it)
            val g = groups[key]

            if (g !== null) {
              emitToGroup(valueSelector, it, g)
            } else {
              if (cancelled) {
                // if the main has been cancelled, stop creating groups
                // and skip this value
                if (groups.isEmpty()) {
                  done = true
                }
              } else {
                val channel = Channel<Any?>(innerBufferSize)
                val group = GroupedFlowImpl<K, V>(
                  key = key,
                  channel = channel,
                  onCancelHandler = {
                    // we send the cancellation event to the main coroutine
                    // to serialize the access to the groups map
                    // and to avoid concurrent modification exceptions.
                    //
                    // use trySend is safe because the channel is unbounded,
                    // and the send is only failed if the channel is closed.
                    groupCancellationChannel
                      .trySend(GroupCancellation(key))
                  }
                )
                groups[key] = group

                try {
                  collector.emit(group)
                } catch (e: CancellationException) {
                  // cancelling the main source means we don't want any more groups
                  // but running groups still require new values
                  cancelled = true

                  // we only kill our subscription to the source if we have
                  // no active groups. As stated above, consider this scenario:
                  // source.groupBy(fn).take(2).
                  if (groups.isEmpty()) {
                    done = true
                  }
                }

                emitToGroup(valueSelector, it, group)
              }
            }
          }
          .onFailure {
            it?.let { throw it }
            done = true
          }
      }
    }

    groupCancellationChannel.close()
    groups.values.forEach { it.close(null) }
  } catch (e: Throwable) {
    groupCancellationChannel.close()
    groups.values.forEach { it.close(e) }

    throw e
  } finally {
    groups.clear()
  }
}
