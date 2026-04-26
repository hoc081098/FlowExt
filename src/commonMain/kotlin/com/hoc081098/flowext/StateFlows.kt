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

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Map a [StateFlow] to another read-only [StateFlow] with the given [transform] function.
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest value of the source [StateFlow].
 * This is useful when you want to map a [StateFlow] to another [StateFlow] instead of a [Flow].
 *
 * @see map
 * @see combineStates
 */
@FlowExtPreview
public fun <T, R> StateFlow<T>.mapState(transform: (value: T) -> R): StateFlow<R> =
  MappedAsStateFlow(this, transform)

/**
 * Combine two [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine two or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  transform: (T1, T2) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, transform),
    valueSupplier = { transform(flow1.value, flow2.value) },
  )

/**
 * Combine three [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine three or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  transform: (T1, T2, T3) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, transform),
    valueSupplier = { transform(flow1.value, flow2.value, flow3.value) },
  )

/**
 * Combine four [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine four or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  transform: (T1, T2, T3, T4) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, transform),
    valueSupplier = { transform(flow1.value, flow2.value, flow3.value, flow4.value) },
  )

/**
 * Combine five [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine five or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  transform: (T1, T2, T3, T4, T5) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, flow5, transform),
    valueSupplier = { transform(flow1.value, flow2.value, flow3.value, flow4.value, flow5.value) },
  )

/**
 * Combine six [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine six or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  transform: (T1, T2, T3, T4, T5, T6) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, flow5, flow6, transform),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
      )
    },
  )

/**
 * Combine seven [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine seven or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  transform: (T1, T2, T3, T4, T5, T6, T7) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7, transform),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
      )
    },
  )

/**
 * Combine eight [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine eight or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  flow8: StateFlow<T8>,
  transform: (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, transform),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
        flow8.value,
      )
    },
  )

/**
 * Combine nine [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine nine or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  flow8: StateFlow<T8>,
  flow9: StateFlow<T9>,
  transform: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, transform),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
        flow8.value,
        flow9.value,
      )
    },
  )

/**
 * Combine ten [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine ten or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  flow8: StateFlow<T8>,
  flow9: StateFlow<T9>,
  flow10: StateFlow<T10>,
  transform: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(
      flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10, transform,
    ),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
        flow8.value,
        flow9.value,
        flow10.value,
      )
    },
  )

/**
 * Combine eleven [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine eleven or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  flow8: StateFlow<T8>,
  flow9: StateFlow<T9>,
  flow10: StateFlow<T10>,
  flow11: StateFlow<T11>,
  transform: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(
      flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10, flow11, transform,
    ),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
        flow8.value,
        flow9.value,
        flow10.value,
        flow11.value,
      )
    },
  )

/**
 * Combine twelve [StateFlow]s into a new read-only [StateFlow] with the given [transform] function..
 *
 * Accessing [StateFlow.value] of the returned [StateFlow] always calls [transform]
 * with the latest values of the source [StateFlow]s.
 * This is useful when you want to combine twelve or more [StateFlow]s into a single [StateFlow] instead of a [Flow].
 *
 * @see combine
 * @see mapState
 */
@FlowExtPreview
public fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> combineStates(
  flow1: StateFlow<T1>,
  flow2: StateFlow<T2>,
  flow3: StateFlow<T3>,
  flow4: StateFlow<T4>,
  flow5: StateFlow<T5>,
  flow6: StateFlow<T6>,
  flow7: StateFlow<T7>,
  flow8: StateFlow<T8>,
  flow9: StateFlow<T9>,
  flow10: StateFlow<T10>,
  flow11: StateFlow<T11>,
  flow12: StateFlow<T12>,
  transform: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R,
): StateFlow<R> =
  DerivedStateFlow(
    source = combine(
      flow1,
      flow2,
      flow3,
      flow4,
      flow5,
      flow6,
      flow7,
      flow8,
      flow9,
      flow10,
      flow11,
      flow12,
      transform,
    ),
    valueSupplier = {
      transform(
        flow1.value,
        flow2.value,
        flow3.value,
        flow4.value,
        flow5.value,
        flow6.value,
        flow7.value,
        flow8.value,
        flow9.value,
        flow10.value,
        flow11.value,
        flow12.value,
      )
    },
  )

// ---------------------------------------- INTERNAL IMPLEMENTATION ----------------------------------------

/**
 * Map a [Flow] to a [StateFlow] with the given [transform] function.
 *
 * Ref: [kotlinx.coroutines/issues/2631](https://github.com/Kotlin/kotlinx.coroutines/issues/2631#issuecomment-870565860)
 */
@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@FlowExtPreview
private class MappedAsStateFlow<T, R>(
  private val source: StateFlow<T>,
  private val transform: (T) -> R,
) : StateFlow<R> {
  override val value: R get() = transform(source.value)
  override val replayCache: List<R> get() = listOf(value)

  override suspend fun collect(collector: FlowCollector<R>): Nothing {
    source
      .map(transform)
      .distinctUntilChanged()
      .collect(collector)

    awaitCancellation()
  }
}

/**
 * Special state flow which value is supplied by [valueSupplier] and collection is delegated to [source]
 * [valueSupplier] should NEVER THROW to avoid contract violation
 *
 * Ref: [kotlinx.coroutines/issues/2631](https://github.com/Kotlin/kotlinx.coroutines/issues/2631#issuecomment-870565860)
 */
@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@FlowExtPreview
private class DerivedStateFlow<T>(
  private val source: Flow<T>,
  private val valueSupplier: () -> T,
) : StateFlow<T> {
  override val value: T get() = valueSupplier()
  override val replayCache: List<T> get() = listOf(value)

  @InternalCoroutinesApi
  override suspend fun collect(collector: FlowCollector<T>): Nothing {
    coroutineScope {
      source
        .stateIn(this)
        .collect(collector)
    }
  }
}
