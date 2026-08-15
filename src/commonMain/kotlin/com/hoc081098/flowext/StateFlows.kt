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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Returns a read-only [StateFlow] derived from this state flow using [transform].
 *
 * Every access to [StateFlow.value] reads the current source value once and invokes [transform] once.
 * The transformed result is not cached between property reads. Every access to [StateFlow.replayCache] performs
 * the same fresh computation and returns the result as a singleton list. Updating the source does not invoke [transform]
 * unless the returned state flow is being collected or one of these properties is accessed.
 *
 * Each collector independently invokes [transform] for source values observed by that collector. Collection follows
 * [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values, and a
 * transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads and
 * collectors do not share transformed results.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [map].
 *
 * @see map
 * @see combineStates
 */
@FlowExtPreview
public fun <T, R> StateFlow<T>.mapState(transform: (value: T) -> R): StateFlow<R> =
  MappedAsStateFlow(this, transform)

/**
 * Returns a read-only [StateFlow] derived from two source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from three source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from four source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from five source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from six source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from seven source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from eight source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from nine source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from ten source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from eleven source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Returns a read-only [StateFlow] derived from twelve source state flows using [transform].
 *
 * Every access to [StateFlow.value] independently reads each source's current value and invokes [transform] once.
 * These reads do not form an atomic snapshot: a source can change between reads, so the supplied values are not
 * guaranteed to have existed simultaneously. The transformed result is not cached between property reads.
 * Every access to [StateFlow.replayCache] performs the same fresh computation and returns the result as a singleton list.
 *
 * Each collector independently invokes [transform] for combined source values observed by that collector. Collection
 * follows [StateFlow]'s strong equality-based conflation: a slow collector can skip intermediate transformed values,
 * and a transformed value that is [equal][Any.equals] to the last emitted value is not emitted again. Property reads
 * and collectors do not share transformed results. Updating a source invokes [transform] only when the returned state
 * flow is being collected; otherwise transformation occurs only when [StateFlow.value] or
 * [StateFlow.replayCache] is accessed.
 *
 * [transform] can be invoked repeatedly and concurrently. To preserve [StateFlow] semantics, it must be
 * deterministic, side-effect-free, safe for concurrent invocation, and must not throw. An exception from
 * [transform] escapes the property access or fails the affected collection; it is not represented as a state value.
 *
 * This operator is useful when a [StateFlow] result is required instead of the [Flow] returned by [combine].
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
 * Computed-on-read [StateFlow] used by [mapState]. Property reads and collectors invoke [transform]
 * independently; this class does not retain transformed values.
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
 * Computed-on-read [StateFlow] whose property values come from [valueSupplier] and whose collection is
 * independently delegated to [source]. [valueSupplier] can be invoked repeatedly and concurrently and must not throw.
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

  override suspend fun collect(collector: FlowCollector<T>): Nothing {
    source
      .conflate()
      .distinctUntilChanged()
      .collect(collector)

    awaitCancellation()
  }
}
