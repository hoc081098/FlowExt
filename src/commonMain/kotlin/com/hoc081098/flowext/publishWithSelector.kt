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

import kotlinx.coroutines.flow.shareIn as kotlinXFlowShareIn
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@FlowExtPreview
@DslMarker
public annotation class PublishSelectorDsl

@FlowExtPreview
@PublishSelectorDsl
public sealed interface SelectorSharedFlowScope<T> {
  @PublishSelectorDsl
  public fun Flow<T>.shared(replay: Int = 0): SharedFlow<T>

  /** @suppress */
  @Deprecated(
    level = DeprecationLevel.ERROR,
    message = "This function is not supported",
    replaceWith = ReplaceWith("this.shared(replay)"),
  )
  public fun <T> Flow<T>.shareIn(
    scope: CoroutineScope,
    started: SharingStarted,
    replay: Int = 0,
  ): SharedFlow<T> = throw UnsupportedOperationException("Not implemented, should not be called")
}

@FlowExtPreview
private typealias SelectorFunction<T, R> = suspend SelectorSharedFlowScope<T>.(Flow<T>) -> Flow<R>

@FlowExtPreview
@PublishSelectorDsl
public sealed interface SelectorScope<T> {
  @PublishSelectorDsl
  public fun <R> select(block: SelectorFunction<T, R>): Flow<R>
}

private class SimpleSuspendLazy<T : Any>(
  initializer: suspend () -> T,
) {
  private val mutex = Mutex()

  private var _initializer: (suspend () -> T)? = initializer

  @Volatile
  private var value: T? = null

  suspend fun getValue(): T =
    value ?: mutex.withLock {
      value ?: _initializer!!().also {
        _initializer = null
        value = it
      }
    }

  fun clear() {
    _initializer = null
    value = null
  }
}

@FlowExtPreview
@OptIn(DelicateCoroutinesApi::class, InternalCoroutinesApi::class)
private class DefaultSelectorScope<T>(
  @JvmField val scope: CoroutineScope,
) : SynchronizedObject(),
  SelectorScope<T>,
  SelectorSharedFlowScope<T> {
  // TODO: atomic
  // Initialized in freezeAndInit
  // Will be set to null when close or cancel
  @JvmField
  @Volatile
  var channels: Array<Channel<T>>? = null

  // TODO: atomic
  // Initialized in freezeAndInit
  // Will be set to null when all output flows are completed.
  @JvmField
  @Volatile
  var cachedSelectedFlows: Array<SimpleSuspendLazy<Flow<Any?>>>? = null

  // TODO: atomic
  @JvmField
  val blocks: MutableList<SelectorFunction<T, Any?>> = ArrayList()

  /**
   * Indicate that this scope is frozen, all [select] calls after this will throw [IllegalStateException].
   */
  @Volatile
  @JvmField
  var isFrozen = false // TODO: atomic

  /**
   * Indicate that a [select] calls is in progress,
   * all [select] calls inside another [select] block will throw [IllegalStateException].
   */
  @JvmField
  @Volatile
  var isInSelectClause = false // TODO: atomic

  // TODO: atomic
  @JvmField
  @Volatile
  var completedCount = 0

  // TODO: atomic or sync?
  override fun <R> select(block: SelectorFunction<T, R>): Flow<R> =
    synchronized(this) {
      check(!isInSelectClause) { "select can not be called inside another select" }
      check(!isFrozen) { "select only can be called inside publish, do not use SelectorScope outside publish" }

      isInSelectClause = true

      blocks += block
      val index = size - 1

      return defer {
        val cached =
          synchronized(this) {
            // Only frozen state can reach here,
            // that means we collect the output flow after frozen this scope
            check(isFrozen) { "only frozen state can reach here!" }

            cachedSelectedFlows
              ?.get(index)
              ?: error("It looks like you are trying to collect the select{} flow outside publish, please don't do that!")
          }

        @Suppress("UNCHECKED_CAST") // Always safe
        cached.getValue() as Flow<R>
      }.onCompletion { onCompleteASelectedFlow(index) }
        .also { isInSelectClause = false }
    }

  // TODO: atomic
  private fun onCompleteASelectedFlow(index: Int) {
    synchronized(this@DefaultSelectorScope) {
      completedCount++

      println("onCompleteASelectedFlow: completedCount = $completedCount, size = $size (index = $index)")

      if (completedCount == size) {
        cachedSelectedFlows?.forEach { it.clear() }
        cachedSelectedFlows = null
        println("onCompleteASelectedFlow: cancel the publish scope")
      }
    }
  }

  override fun Flow<T>.shared(replay: Int): SharedFlow<T> =
    kotlinXFlowShareIn(
      scope = scope,
      started = SharingStarted.Lazily,
      replay = replay,
    )

  // TODO: synchronized?
  fun freezeAndInit() =
    synchronized(this) {
      val channels = Array(size) { Channel<T>() }.also { this.channels = it }

      cachedSelectedFlows =
        Array(size) { index ->
          val block = blocks[index]
          val flow = channels[index].consumeAsFlow()

          SimpleSuspendLazy { this.block(flow) }
        }

      isFrozen = true
    }

  private inline val size: Int get() = blocks.size

  suspend fun send(value: T) {
    println("send: $value")
    for (channel in channels.orEmpty()) {
      if (channel.isClosedForSend || channel.isClosedForReceive) {
        continue
      }

      try {
        channel.send(value)
      } catch (_: Throwable) {
        // Swallow all exceptions
      }
    }
  }

  // TODO: synchronized?
  fun close(e: Throwable?) =
    synchronized(this) {
      println("close: $e")
      for (channel in channels.orEmpty()) {
        channel.close(e)
      }
      channels = null
    }

  // TODO: synchronized?
  fun cancel(e: CancellationException) =
    synchronized(this) {
      println("cancel: $e")
      for (channel in channels.orEmpty()) {
        channel.cancel(e)
      }
      channels = null
    }
}

@FlowExtPreview
public fun <T, R> Flow<T>.publish(selector: suspend SelectorScope<T>.() -> Flow<R>): Flow<R> {
  val source = this

  return flow {
    coroutineScope {
      val scope = DefaultSelectorScope<T>(this)

      val output = selector(scope)

      // IMPORTANT: freeze and init before collect the output flow
      scope.freezeAndInit()

      launch {
        try {
          source.collect { value -> return@collect scope.send(value) }
          scope.close(null)
        } catch (e: CancellationException) {
          scope.cancel(e)
          throw e
        } catch (e: Throwable) {
          scope.close(e)
          throw e
        }
      }

      // IMPORTANT: collect the output flow after frozen the scope
      emitAll(output)
    }
  }
}

@OptIn(FlowExtPreview::class, ExperimentalCoroutinesApi::class)
public suspend fun main() {
  flow<Any?> {
    println("Collect...")
    delay(100)
    emit(1)
    delay(100)
    emit(2)
    delay(100)
    emit(3)
    delay(100)
    emit("4")
  }.onEach { println(">>> onEach: $it") }
    .publish {
      delay(100)

      merge(
        select { flow ->
          delay(1)
          val sharedFlow = flow.shared()

          interval(0, 100)
            .onEach { println(">>> interval: $it") }
            .flatMapMerge { value ->
              timer(value, 50)
                .withLatestFrom(sharedFlow)
                .map { it to "shared" }
            }.takeUntil(sharedFlow.filter { it == 3 })
        },
        select { flow ->
          flow
            .filterIsInstance<Int>()
            .filter { it % 2 == 0 }
            .map { it to "even" }
            .take(1)
        },
        select { flow ->
          flow
            .filterIsInstance<Int>()
            .filter { it % 2 != 0 }
            .map { it to "odd" }
            .take(1)
        },
        select { flow ->
          flow
            .filterIsInstance<String>()
            .map { it to "string" }
            .take(1)
        },
      )
    }
    .toList()
    .also { println(it) }
    .let {
      check(it == listOf(Pair(1, "odd"), Pair(2, "even"), Pair("4", "string")))
    }
}
