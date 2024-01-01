/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Petrus Nguyễn Thái Học
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
import com.hoc081098.flowext.internal.identitySuspendFunction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
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
 * @param bufferSize bufferSize hints about the number of expected values from each inner [GroupedFlow].
 * Should be either a positive channel capacity or one of the constants defined in [Channel].
 * If inner [GroupedFlow] tries to emit more than [bufferSize] values before it is collected,
 * it will be suspended. See [SendChannel.send] for details.
 * @param keySelector a function that extracts the key for each item
 * @param valueSelector a function that extracts the return value for each item
 * @see GroupedFlow
 */
@FlowExtPreview
@ExperimentalCoroutinesApi
public fun <T, K, V> Flow<T>.groupBy(
  bufferSize: Int = Channel.BUFFERED,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
): Flow<GroupedFlow<K, V>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = valueSelector,
  bufferSize = bufferSize,
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
 * @param bufferSize bufferSize hints about the number of expected values from each inner [GroupedFlow].
 * Should be either a positive channel capacity or one of the constants defined in [Channel].
 * If inner [GroupedFlow] tries to emit more than [bufferSize] values before it is collected,
 * it will be suspended. See [SendChannel.send] for details.
 * @param keySelector a function that extracts the key for each item
 * @see GroupedFlow
 */
@Suppress("UNCHECKED_CAST")
@FlowExtPreview
@ExperimentalCoroutinesApi
public fun <T, K> Flow<T>.groupBy(
  bufferSize: Int = Channel.BUFFERED,
  keySelector: suspend (T) -> K,
): Flow<GroupedFlow<K, T>> = groupByInternal(
  source = this,
  keySelector = keySelector,
  valueSelector = identitySuspendFunction as (suspend (T) -> T),
  bufferSize = bufferSize,
)

@ExperimentalCoroutinesApi
private class GroupedFlowImpl<K, V>(
  override val key: K,
  private val channel: Channel<V>,
  private val onCancelHandler: (self: GroupedFlowImpl<K, V>) -> Unit,
) : GroupedFlow<K, V>, Flow<V> {
  private val consumed = AtomicBoolean()

  @Suppress("NOTHING_TO_INLINE")
  private inline fun markConsumed() {
    check(!consumed.getAndSet(true)) { "A GroupedFlow can be collected just once" }
  }

  @OptIn(DelicateCoroutinesApi::class)
  fun isActive() = !channel.isClosedForSend && !channel.isClosedForReceive

  suspend fun send(element: V) {
    check(isActive()) { "GroupedFlowImpl is already completed" }
    channel.send(element)
  }

  fun close(cause: Throwable?) {
    // called when main flow is completed or throws an exception.
    // do not check done.value here because we may store this GroupedFlowImpl in the map.
    channel.close(cause)
  }

  override suspend fun collect(collector: FlowCollector<V>) {
    markConsumed()

    var cause: Throwable? = null
    try {
      for (element in channel) {
        collector.emit(element)
      }
    } catch (e: Throwable) {
      cause = e
      throw e
    } finally {
      // call the onCancelHandler to remove the GroupedFlowImpl from the map
      onCancelHandler(this)

      // cancel the channel to unblock the senders
      channel.cancelConsumed(cause)
    }
  }

  override fun toString() = "${super.toString()}(key=$key, channel=$channel)"
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ReceiveChannel<*>.cancelConsumed(cause: Throwable?) {
  cancel(
    cause?.let {
      it as? CancellationException
        ?: CancellationException("Channel was consumed, consumer had failed", it)
    },
  )
}

@Suppress("NOTHING_TO_INLINE")
@ExperimentalCoroutinesApi
private suspend inline fun <K, T, V> emitToGroup(
  crossinline valueSelector: suspend (T) -> V,
  value: T,
  group: GroupedFlowImpl<K, V>,
) = group.send(valueSelector(value))

@FlowExtPreview
@ExperimentalCoroutinesApi
private fun <T, K, V> groupByInternal(
  source: Flow<T>,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
  bufferSize: Int,
): Flow<GroupedFlow<K, V>> = flow {
  val collector = this

  val groups = linkedMapOf<K, GroupedFlowImpl<K, V>>()
  val cancelledGroupChannel = Channel<GroupedFlowImpl<K, V>>(Channel.UNLIMITED)

  try {
    coroutineScope {
      // Produce the values using the default (rendezvous) channel
      val values = produce {
        source.collect { send(it) }
      }

      var done = false
      var cancelled = false

      /**
       * Drains the cancelledGroupChannel until it is empty or closed or failed.
       * @return true should stop the outer loop, false otherwise.
       */
      fun drainCancelledGroupChannel(): Boolean {
        while (true) {
          cancelledGroupChannel
            .tryReceive()
            .onSuccess { group ->
              // remove the group from the map,
              // we must check both key and value to be sure that we remove the right group
              // because new group with the same key may be created while old group becomes inactive
              // we don't want to remove the new group.
              groups.removeEntry(group.key, group)

              // and check if we have no more groups and the main has been cancelled
              // to stop the loop.
              if (groups.isEmpty() && cancelled) {
                return true
              }
            }
            .onFailure { return false }
        }
      }

      outer@ while (!done) {
        // receive groupCancellationChannel until it is empty or channel is closed or failed.
        if (drainCancelledGroupChannel()) {
          break@outer
        }

        // now, we can receive from the values channel
        values
          .receiveCatching()
          .onSuccess { value ->
            val key = keySelector(value)

            if (drainCancelledGroupChannel()) {
              // will exit the outer loop
              done = true
              return@onSuccess
            }

            val g = groups[key]
            if (g !== null) {
              // only emit to the group if it is active
              if (g.isActive()) {
                emitToGroup(valueSelector, value, g)
                // continue the outer loop
                return@onSuccess
              }

              // if the group is not active, remove it from the map,
              // and create a new one instead.
              groups.remove(key)
            }

            if (cancelled) {
              // if the main has been cancelled, stop creating groups
              // and skip this value
              if (groups.isEmpty()) {
                done = true
              }
            } else {
              val group = GroupedFlowImpl(
                key = key,
                channel = Channel(bufferSize),
                // we send the cancellation event to the main coroutine
                // to serialize the access to the groups map
                // and to avoid concurrent modification exceptions.
                //
                // use trySend is safe because the channel is unbounded,
                // and the send is only failed if the channel is closed.
                onCancelHandler = cancelledGroupChannel::trySend,
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

              // FIXME: do we need to check if the group is active here?
              emitToGroup(valueSelector, value, group)
            }
          }
          .onFailure {
            it?.let { throw it }
            done = true
          }
      }
    }

    // must close cancelledGroupChannel before closing the groups
    cancelledGroupChannel.close()
    groups.values.forEach { it.close(null) }
  } catch (e: Throwable) {
    // must close cancelledGroupChannel before closing the groups
    cancelledGroupChannel.close()
    groups.values.forEach { it.close(e) }

    throw e
  } finally {
    groups.clear()
  }
}

/**
 * Removes [key] from map if it is mapped to [value].
 */
private fun <Key, Value> MutableMap<Key, Value>.removeEntry(key: Key, value: Value): Boolean {
  if (this[key] != value) return false
  this.remove(key)
  return true
}
