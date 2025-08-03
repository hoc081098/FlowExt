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

import com.hoc081098.flowext.internal.AtomicRef
import com.hoc081098.flowext.internal.SimpleLazy
import com.hoc081098.flowext.internal.SimpleSuspendLazy
import com.hoc081098.flowext.internal.loop
import com.hoc081098.flowext.internal.simpleLazyOf
import com.hoc081098.flowext.internal.simpleSuspendLazy
import kotlin.jvm.JvmField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.flow.shareIn as kotlinXFlowShareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@FlowExtPreview
@DslMarker
public annotation class PublishSelectorDsl

@FlowExtPreview
@DslMarker
public annotation class PublishSelectorSharedFlowDsl

@FlowExtPreview
@PublishSelectorSharedFlowDsl
public sealed interface SelectorSharedFlowScope<T> {
  @PublishSelectorSharedFlowDsl
  public fun Flow<T>.shareIn(
    replay: Int = 0,
    started: SharingStarted = SharingStarted.Lazily,
  ): SharedFlow<T>

  /** @suppress */
  @Deprecated(
    level = DeprecationLevel.ERROR,
    message = "This function is not supported",
    replaceWith = ReplaceWith("this.shareIn(replay, started)"),
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

private typealias SimpleSuspendLazyOfAnyFlow = SimpleSuspendLazy<Flow<Any?>>

@FlowExtPreview
private sealed interface SelectorScopeState<out T> {
  data object Init : SelectorScopeState<Nothing>

  sealed interface NotFrozen<T> : SelectorScopeState<T> {
    val blocks: List<SelectorFunction<T, Any?>>

    data class InSelectClause<T>(
      override val blocks: List<SelectorFunction<T, Any?>>,
    ) : NotFrozen<T>

    data class NotInSelectClause<T>(
      override val blocks: List<SelectorFunction<T, Any?>>,
    ) : NotFrozen<T>
  }

  data class Frozen<T>(
    val selectedFlowsAndChannels: SimpleLazy<
      Pair<
        List<SimpleSuspendLazyOfAnyFlow>,
        List<Channel<T>>,
        >,
      >,
    val completedCount: Int,
    val blocks: List<SelectorFunction<T, Any?>>,
  ) : SelectorScopeState<T>

  data object Closed : SelectorScopeState<Nothing>
}

@FlowExtPreview
@OptIn(DelicateCoroutinesApi::class)
private class DefaultSelectorScope<T>(
  @JvmField val scope: CoroutineScope,
) : SelectorScope<T>,
  SelectorSharedFlowScope<T> {
  @JvmField
  val stateRef = AtomicRef<SelectorScopeState<T>>(SelectorScopeState.Init)

  override fun <R> select(block: SelectorFunction<T, R>): Flow<R> {
    println("call select with block: $block")

    stateRef.loop { state ->
      val updated = when (state) {
        SelectorScopeState.Closed -> {
          error("This scope is closed")
        }

        is SelectorScopeState.Frozen -> {
          error("This scope is frozen. `select` only can be called inside `publish`, do not use `SelectorScope` outside `publish`")
        }

        SelectorScopeState.Init -> {
          // Ok, lets transition to NotFrozen.InSelectClause
          SelectorScopeState.NotFrozen.InSelectClause(blocks = listOf(block))
        }

        is SelectorScopeState.NotFrozen.InSelectClause -> {
          error("`select` can not be called inside another `select`")
        }

        is SelectorScopeState.NotFrozen.NotInSelectClause -> {
          // Ok, lets transition to NotFrozen.InSelectClause
          SelectorScopeState.NotFrozen.InSelectClause(blocks = state.blocks + block)
        }
      }

      if (stateRef.compareAndSet(expect = state, update = updated)) {
        // CAS success
        val index = updated.blocks.size - 1

        val result = defer {
          // Only frozen state can reach here,
          // that means we collect the output flow after frozen this scope
          val stateWhenCollecting = stateRef.value
          check(stateWhenCollecting is SelectorScopeState.Frozen) { "only frozen state can reach here!" }

          @Suppress("UNCHECKED_CAST") // We know that the type is correct
          stateWhenCollecting
            .selectedFlowsAndChannels
            .getValue()
            .first[index]
            .getValue()
            as Flow<R>
        }.onCompletion { onCompleteSelectedFlow(index) }

        transitionToNotInSelectClause()

        return result
      }
    }
  }

  private fun transitionToNotInSelectClause() {
    stateRef.loop { state ->
      val updated = when (state) {
        is SelectorScopeState.NotFrozen.InSelectClause -> {
          // Ok, lets transition to NotFrozen.NotInSelectClause
          SelectorScopeState.NotFrozen.NotInSelectClause(blocks = state.blocks)
        }

        is SelectorScopeState.NotFrozen.NotInSelectClause -> {
          // Ok, state already is NotFrozen.NotInSelectClause
          return
        }

        SelectorScopeState.Closed -> {
          error("This scope is closed")
        }

        is SelectorScopeState.Frozen -> {
          error("This scope is frozen. `select` only can be called inside `publish`, do not use `SelectorScope` outside `publish`")
        }

        SelectorScopeState.Init -> {
          error("Cannot be here!")
        }
      }

      if (stateRef.compareAndSet(expect = state, update = updated)) {
        // CAS success
        return
      }
    }
  }

  private fun onCompleteSelectedFlow(index: Int) {
    stateRef.loop { state ->
      val updated = when (state) {
        SelectorScopeState.Init -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.NotFrozen.InSelectClause -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.NotFrozen.NotInSelectClause -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.Frozen -> {
          if (state.completedCount == state.blocks.size) {
            // Ok, all output flows are completed. Lets transition to DefaultSelectorScopeState.Closed
            SelectorScopeState.Closed
          } else {
            // Ok, lets transition to DefaultSelectorScopeState.Frozen with completedCount=completedCount + 1
            state.copy(completedCount = state.completedCount + 1)
          }
        }

        SelectorScopeState.Closed -> {
          // Ok, already closed. Do nothing.
          return
        }
      }

      if (stateRef.compareAndSet(expect = state, update = updated)) {
        // CAS success

        println(
          "onCompleteSelectedFlow: completedCount = ${(updated as? SelectorScopeState.Frozen)?.completedCount}, " +
            "size = ${state.blocks.size}, " +
            "(index = $index)",
        )

        // Once state reaches DefaultSelectorScopeState.Closed, we can clear unused lazy
        if (updated is SelectorScopeState.Closed) {
          state.selectedFlowsAndChannels.run {
            getOrNull()?.first?.forEach { it.clear() }
            clear()
          }

          println("onCompleteSelectedFlow: cancel the publish scope")
        }

        return
      }
    }
  }

  override fun Flow<T>.shareIn(replay: Int, started: SharingStarted): SharedFlow<T> =
    kotlinXFlowShareIn(
      scope = scope,
      started = started,
      replay = replay,
    )

  fun freezeAndInit() {
    stateRef.loop { state ->
      // Transition from NotFrozen to Frozen
      when (state) {
        SelectorScopeState.Init -> {
          error("Not implemented")
        }

        is SelectorScopeState.NotFrozen<T> -> {
          // Freeze and init
        }

        is SelectorScopeState.Frozen<T>, SelectorScopeState.Closed -> {
          // Already frozen or closed
          return
        }
      }

      val blocks = state.blocks
      val size = blocks.size
      val updated = SelectorScopeState.Frozen(
        selectedFlowsAndChannels = simpleLazyOf {
          val channels = List(size) { Channel<T>() }

          List(size) { index ->
            val block = blocks[index]
            val flow = channels[index].consumeAsFlow()
            simpleSuspendLazy { this.block(flow) }
          } to channels
        },
        completedCount = 0,
        blocks = blocks,
      )

      if (stateRef.compareAndSet(expect = state, update = updated)) {
        // CAS success
        return
      }

      // clear unused lazy
      updated.selectedFlowsAndChannels.clear()
    }
  }

  suspend fun send(value: T) {
    println("send: $value")

    val state = stateRef.value as? SelectorScopeState.Frozen<T> ?: return
    val channels = state
      .selectedFlowsAndChannels
      .getValue()
      .second

    for (channel in channels) {
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

  private fun transitionToClosed(action: (Channel<T>) -> Unit) {
    stateRef.loop { state ->
      val updated = when (state) {
        SelectorScopeState.Init -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.NotFrozen.InSelectClause -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.NotFrozen.NotInSelectClause -> {
          error("Cannot be here!")
        }

        is SelectorScopeState.Frozen -> {
          // Ok, lets transition to DefaultSelectorScopeState.Closed
          SelectorScopeState.Closed
        }

        SelectorScopeState.Closed -> {
          // Ok, already closed. Do nothing.
          return
        }
      }

      if (stateRef.compareAndSet(expect = state, update = updated)) {
        // CAS success

        // Once state reaches DefaultSelectorScopeState.Closed, we can clear unused lazy and close all channels
        state.selectedFlowsAndChannels.run {
          getOrNull()?.first?.forEach { it.clear() }
          getOrNull()?.second?.forEach(action)
          clear()
        }

        return
      }
    }
  }

  fun close(e: Throwable?) {
    println("close: $e")
    transitionToClosed { it.close(e) }
  }

  fun cancel(e: CancellationException) {
    println("cancel: $e")
    transitionToClosed { it.cancel(e) }
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
          val sharedFlow = flow.shareIn()

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
    .let { check(it == listOf(Pair(1, "odd"), Pair(2, "even"), Pair("4", "string"))) }
}
