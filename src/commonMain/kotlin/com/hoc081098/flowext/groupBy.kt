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
import kotlinx.coroutines.CancellationException
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
 */
public interface GroupedFlow<K, T> : Flow<T> {
  /**
   * The key of this Flow.
   */
  public val key: K
}

@ExperimentalCoroutinesApi
public fun <T, K, V> Flow<T>.groupBy(
  innerBufferSize: Int = Channel.RENDEZVOUS,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V
): Flow<GroupedFlow<K, V>> = groupByInternal2(
  source = this,
  keySelector = keySelector,
  valueSelector = valueSelector,
  innerBufferSize = innerBufferSize
)

@ExperimentalCoroutinesApi
public fun <T, K> Flow<T>.groupBy(
  innerBufferSize: Int = Channel.RENDEZVOUS,
  keySelector: suspend (T) -> K
): Flow<GroupedFlow<K, T>> = groupByInternal2(
  source = this,
  keySelector = keySelector,
  valueSelector = { it },
  innerBufferSize = innerBufferSize
)

@ExperimentalCoroutinesApi
private class GroupedFlowImpl<K, V>(
  override val key: K,
  private val channel: Channel<V>,
  private val onCloseHandler: (cause: Throwable?) -> Unit
) : GroupedFlow<K, V>, Flow<V> {
  private val calledOnCloseHandler = AtomicBoolean()

  init {
    channel.invokeOnClose { throwable ->
      if (calledOnCloseHandler.compareAndSet(expect = false, update = true)) {
        onCloseHandler(throwable)
      }
    }
  }

  suspend fun send(element: V) = channel.send(element)

  fun close(cause: Throwable?) = channel.close(cause)

  override suspend fun collect(collector: FlowCollector<V>) =
    channel.consumeEach { collector.emit(it) }

  override fun toString() = "${super.toString()}(key=$key, channel=$channel)"
}

// private fun <T, K, V> groupByInternal(
//  source: Flow<T>,
//  keySelector: suspend (T) -> K,
//  valueSelector: suspend (T) -> V,
//  innerBufferSize: Int
// ): Flow<GroupedFlow<K, V>> = flow {
//  val collector = this
//
//  val groups = ConcurrentHashMap<K, GroupedFlowImpl<K, V>>()
//  val cancelled = AtomicBoolean()
//  val completed = AtomicBoolean()
//  val teardown = CompletableDeferred<Nothing>()
//
//  try {
//    coroutineScope {
//      val job = launch(start = CoroutineStart.UNDISPATCHED) { teardown.await() }
//
//      source.collect { value ->
//        val key = keySelector(value)
//        val g = groups[key]
//
//        if (g !== null) {
//          emitToGroup(valueSelector, value, g)
//        } else {
//          if (cancelled.value) {
//            // if the main has been cancelled, stop creating groups
//            // and skip this value
//            if (groups.isEmpty()) {
//              throw ClosedException(collector)
//            }
//          } else {
//            val group = GroupedFlowImpl(
//              key = key,
//              channel = Channel<V>(innerBufferSize),
//              onCompletion = {
//                println("onCompletion  group $key")
//
//                if (!completed.value) {
//                  if (
//                    groups.remove(key) != null &&
//                    groups.isEmpty() &&
//                    cancelled.value
//                  ) {
//                    println("onCompletion group $key cancelled")
//                    teardown.completeExceptionally(ClosedException(collector))
//                  } else {
//                    println("onCompletion group $key not yet")
//                  }
//                } else {
//                  println("onCompletion group $key, completed ignored..")
//                }
//              }
//            )
//            groups[key] = group
//
//            try {
//              collector.emit(group)
//            } catch (e: CancellationException) {
//              // cancelling the main source means we don't want any more groups
//              // but running groups still require new values
//              cancelled.value = true
//
//              // we only kill our subscription to the source if we have
//              // no active groups. As stated above, consider this scenario:
//              // source.groupBy(fn).take(2).
//              if (groups.isEmpty()) {
//                throw ClosedException(collector)
//              }
//            }
//
//            emitToGroup(valueSelector, value, group)
//          }
//        }
//      }
//
//      job.cancel()
//
//      if (completed.compareAndSet(expect = false, update = true)) {
//        println("normal completion 1")
//        groups.forEach { it.value.close(null) }
//        println("normal completion 2")
//      }
//    }
//  } catch (e: ClosedException) {
//    if (completed.compareAndSet(expect = false, update = true)) {
//      println("closed $e ${e.owner === collector} 1")
//
//      if (e.owner === collector) {
//        groups.forEach { it.value.close(null) }
//        println("closed $e ${e.owner === collector} 2")
//      } else {
//        groups.forEach { it.value.close(e) }
//        println("closed $e ${e.owner === collector} 3")
//        throw e
//      }
//    }
//  } catch (e: Throwable) {
//    if (completed.compareAndSet(expect = false, update = true)) {
//      println("error $e 1")
//      e.printStackTrace()
//      groups.forEach { it.value.close(e) }
//      println("error $e 2")
//      throw e
//    }
//  } finally {
//    println("finally clearing...")
//    groups.clear()
//    println("finally cleared")
//  }
// }

@ExperimentalCoroutinesApi
private suspend inline fun <K, T, V> emitToGroup(
  crossinline valueSelector: suspend (T) -> V,
  value: T,
  group: GroupedFlowImpl<K, V>
) = group
  .also { println(" --> sending... $value to group ${group.key}") }
  .send(valueSelector(value))
  .also { println(" <-- sent $value to group ${group.key}") }

private class GroupCancellation(val key: Any?) {
  override fun toString(): String = "GroupCancellation(key=$key)"
}

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
private fun <T, K, V> groupByInternal2(
  source: Flow<T>,
  keySelector: suspend (T) -> K,
  valueSelector: suspend (T) -> V,
  innerBufferSize: Int
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

      suspend fun handle(value: T) {
        val key = keySelector(value)
        val g = groups[key]

        if (g !== null) {
          emitToGroup(valueSelector, value, g)
        } else {
          if (cancelled) {
            // if the main has been cancelled, stop creating groups
            // and skip this value
            if (groups.isEmpty()) {
              done = true
            }
          } else {
            val channel = Channel<V>(innerBufferSize)
            val group = GroupedFlowImpl(
              key = key,
              channel = channel,
              onCloseHandler = { throwable ->
                println("    [group] onClose group $key throwable=$throwable sending...")

                // we send the cancellation event to the main coroutine
                // to serialize the access to the groups map
                // and to avoid concurrent modification exceptions.
                //
                // use trySend is safe because the channel is unbounded,
                // and the send is only failed if the channel is closed.
                groupCancellationChannel
                  .trySend(GroupCancellation(key))
                  .also { println("    [group] onClose group $key sent $it") }
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

            emitToGroup(valueSelector, value, group)
          }
        }
      }

      fun handleCancellation(event: GroupCancellation) {
        println("[main] received $event")
        val key = event.key as K

        // remove the group from the map
        // and check if we have no more groups and the main has been cancelled
        // to stop the loop.
        groups.remove(key)
        if (groups.isEmpty() && cancelled) {
          done = true
        }
      }

      while (!done) {
        groupCancellationChannel
          .tryReceive()
          .also { println("[main] received gr=$it") }
          .onSuccess { handleCancellation(it) }
          .onFailure {
            values.receiveCatching()
              .also { println("[main] received $it") }
              .onSuccess { handle(it) }
              .onFailure {
                it?.let { throw it }
                done = true
              }
          }
      }
    }

    println("normal completion 1")
    groupCancellationChannel.close()
    groups.values.forEach { it.close(null) }

    println("normal completion 2")
  } catch (e: Throwable) {
    println("error completion $e")

    groupCancellationChannel.close()
    groups.values.forEach { it.close(e) }

    throw e
  } finally {
    println("finally clearing...")
    groups.clear()
    println("finally cleared")
  }
}
