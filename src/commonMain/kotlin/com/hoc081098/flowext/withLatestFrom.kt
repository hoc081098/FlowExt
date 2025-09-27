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

import com.hoc081098.flowext.internal.AtomicRef
import com.hoc081098.flowext.internal.INTERNAL_NULL_VALUE
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Merges two [Flow]s into one [Flow] by combining each value from self with the latest value from the second [Flow], if any.
 * Values emitted by self before the second [Flow] has emitted any values will be omitted.
 *
 * @param other Second [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest value from the second [Flow], if any.
 */
public fun <A, B, R> Flow<A>.withLatestFrom(
  other: Flow<B>,
  transform: suspend (A, B) -> R,
): Flow<R> {
  return flow {
    val otherRef = AtomicRef<Any?>(null)

    try {
      coroutineScope {
        val otherCollectionJob = launch(start = CoroutineStart.UNDISPATCHED) {
          other.collect { otherRef.value = it ?: INTERNAL_NULL_VALUE }
        }

        collect { value ->
          emit(
            transform(
              value,
              INTERNAL_NULL_VALUE.unbox(otherRef.value ?: return@collect),
            ),
          )
        }
        otherCollectionJob.cancelAndJoin()
      }
    } finally {
      otherRef.value = null
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun <A, B> Flow<A>.withLatestFrom(other: Flow<B>): Flow<Pair<A, B>> =
  withLatestFrom(other, ::Pair)

/**
 * Merges three [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  transform: suspend (A, B, C) -> R,
): Flow<R> {
  return flow {
    val other2Ref = AtomicRef<Any?>(null)
    val other3Ref = AtomicRef<Any?>(null)

    try {
      coroutineScope {
        val other2Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other2.collect { other2Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other3Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other3.collect { other3Ref.value = it ?: INTERNAL_NULL_VALUE }
        }

        collect { value ->
          val value2 = other2Ref.value ?: return@collect
          val value3 = other3Ref.value ?: return@collect
          emit(
            transform(
              value,
              INTERNAL_NULL_VALUE.unbox(value2),
              INTERNAL_NULL_VALUE.unbox(value3),
            ),
          )
        }
        other2Job.cancelAndJoin()
        other3Job.cancelAndJoin()
      }
    } finally {
      other2Ref.value = null
      other3Ref.value = null
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun <A, B, C> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
): Flow<Triple<A, B, C>> = withLatestFrom(other2, other3, ::Triple)

/**
 * Merges four [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  transform: suspend (A, B, C, D) -> R,
): Flow<R> {
  return flow {
    val other2Ref = AtomicRef<Any?>(null)
    val other3Ref = AtomicRef<Any?>(null)
    val other4Ref = AtomicRef<Any?>(null)

    try {
      coroutineScope {
        val other2Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other2.collect { other2Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other3Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other3.collect { other3Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other4Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other4.collect { other4Ref.value = it ?: INTERNAL_NULL_VALUE }
        }

        collect { value ->
          val value2 = other2Ref.value ?: return@collect
          val value3 = other3Ref.value ?: return@collect
          val value4 = other4Ref.value ?: return@collect
          emit(
            transform(
              value,
              INTERNAL_NULL_VALUE.unbox(value2),
              INTERNAL_NULL_VALUE.unbox(value3),
              INTERNAL_NULL_VALUE.unbox(value4),
            ),
          )
        }
        other2Job.cancelAndJoin()
        other3Job.cancelAndJoin()
        other4Job.cancelAndJoin()
      }
    } finally {
      other2Ref.value = null
      other3Ref.value = null
      other4Ref.value = null
    }
  }
}

/**
 * Merges five [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  transform: suspend (A, B, C, D, E) -> R,
): Flow<R> {
  return flow {
    val other2Ref = AtomicRef<Any?>(null)
    val other3Ref = AtomicRef<Any?>(null)
    val other4Ref = AtomicRef<Any?>(null)
    val other5Ref = AtomicRef<Any?>(null)

    try {
      coroutineScope {
        val other2Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other2.collect { other2Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other3Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other3.collect { other3Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other4Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other4.collect { other4Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other5Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other5.collect { other5Ref.value = it ?: INTERNAL_NULL_VALUE }
        }

        collect { value ->
          val value2 = other2Ref.value ?: return@collect
          val value3 = other3Ref.value ?: return@collect
          val value4 = other4Ref.value ?: return@collect
          val value5 = other5Ref.value ?: return@collect
          emit(
            transform(
              value,
              INTERNAL_NULL_VALUE.unbox(value2),
              INTERNAL_NULL_VALUE.unbox(value3),
              INTERNAL_NULL_VALUE.unbox(value4),
              INTERNAL_NULL_VALUE.unbox(value5),
            ),
          )
        }
        other2Job.cancelAndJoin()
        other3Job.cancelAndJoin()
        other4Job.cancelAndJoin()
        other5Job.cancelAndJoin()
      }
    } finally {
      other2Ref.value = null
      other3Ref.value = null
      other4Ref.value = null
      other5Ref.value = null
    }
  }
}

/**
 * Merges six [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param other6 Sixth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, F, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  other6: Flow<F>,
  transform: suspend (A, B, C, D, E, F) -> R,
): Flow<R> {
  return flow {
    val other2Ref = AtomicRef<Any?>(null)
    val other3Ref = AtomicRef<Any?>(null)
    val other4Ref = AtomicRef<Any?>(null)
    val other5Ref = AtomicRef<Any?>(null)
    val other6Ref = AtomicRef<Any?>(null)

    try {
      coroutineScope {
        val other2Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other2.collect { other2Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other3Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other3.collect { other3Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other4Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other4.collect { other4Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other5Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other5.collect { other5Ref.value = it ?: INTERNAL_NULL_VALUE }
        }
        val other6Job = launch(start = CoroutineStart.UNDISPATCHED) {
          other6.collect { other6Ref.value = it ?: INTERNAL_NULL_VALUE }
        }

        collect { value ->
          val value2 = other2Ref.value ?: return@collect
          val value3 = other3Ref.value ?: return@collect
          val value4 = other4Ref.value ?: return@collect
          val value5 = other5Ref.value ?: return@collect
          val value6 = other6Ref.value ?: return@collect
          emit(
            transform(
              value,
              INTERNAL_NULL_VALUE.unbox(value2),
              INTERNAL_NULL_VALUE.unbox(value3),
              INTERNAL_NULL_VALUE.unbox(value4),
              INTERNAL_NULL_VALUE.unbox(value5),
              INTERNAL_NULL_VALUE.unbox(value6),
            ),
          )
        }
        other2Job.cancelAndJoin()
        other3Job.cancelAndJoin()
        other4Job.cancelAndJoin()
        other5Job.cancelAndJoin()
        other6Job.cancelAndJoin()
      }
    } finally {
      other2Ref.value = null
      other3Ref.value = null
      other4Ref.value = null
      other5Ref.value = null
      other6Ref.value = null
    }
  }
}
