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
 * Merges multiple [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param others Array of other [Flow]s
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, R> Flow<A>.withLatestFrom(
  others: Array<out Flow<*>>,
  transform: suspend (A, Array<Any?>) -> R,
): Flow<R> {
  return flow {
    val refs = Array<AtomicRef<Any?>>(others.size) { AtomicRef(null) }

    try {
      coroutineScope {
        val jobs = others.mapIndexed { index, flow ->
          launch(start = CoroutineStart.UNDISPATCHED) {
            flow.collect { refs[index].value = it ?: INTERNAL_NULL_VALUE }
          }
        }

        collect { value ->
          val values = Array<Any?>(refs.size) { index ->
            refs[index].value ?: return@collect
          }
          
          val unboxedValues = Array<Any?>(values.size) { index ->
            INTERNAL_NULL_VALUE.unbox(values[index])
          }
          
          emit(transform(value, unboxedValues))
        }
        
        jobs.forEach { it.cancelAndJoin() }
      }
    } finally {
      refs.forEach { it.value = null }
    }
  }
}

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
  return withLatestFrom(arrayOf(other2, other3)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
    )
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
  return withLatestFrom(arrayOf(other2, other3, other4)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
    )
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
  return withLatestFrom(arrayOf(other2, other3, other4, other5)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
    )
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
  return withLatestFrom(arrayOf(other2, other3, other4, other5, other6)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
      others[4] as F,
    )
  }
}

/**
 * Merges seven [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param other6 Sixth [Flow]
 * @param other7 Seventh [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, F, G, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  other6: Flow<F>,
  other7: Flow<G>,
  transform: suspend (A, B, C, D, E, F, G) -> R,
): Flow<R> {
  return withLatestFrom(arrayOf(other2, other3, other4, other5, other6, other7)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
      others[4] as F,
      others[5] as G,
    )
  }
}

/**
 * Merges eight [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param other6 Sixth [Flow]
 * @param other7 Seventh [Flow]
 * @param other8 Eighth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, F, G, H, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  other6: Flow<F>,
  other7: Flow<G>,
  other8: Flow<H>,
  transform: suspend (A, B, C, D, E, F, G, H) -> R,
): Flow<R> {
  return withLatestFrom(arrayOf(other2, other3, other4, other5, other6, other7, other8)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
      others[4] as F,
      others[5] as G,
      others[6] as H,
    )
  }
}

/**
 * Merges nine [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param other6 Sixth [Flow]
 * @param other7 Seventh [Flow]
 * @param other8 Eighth [Flow]
 * @param other9 Ninth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, F, G, H, I, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  other6: Flow<F>,
  other7: Flow<G>,
  other8: Flow<H>,
  other9: Flow<I>,
  transform: suspend (A, B, C, D, E, F, G, H, I) -> R,
): Flow<R> {
  return withLatestFrom(arrayOf(other2, other3, other4, other5, other6, other7, other8, other9)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
      others[4] as F,
      others[5] as G,
      others[6] as H,
      others[7] as I,
    )
  }
}

/**
 * Merges ten [Flow]s into one [Flow] by combining each value from self with the latest values from the other [Flow]s, if any.
 * Values emitted by self before all other [Flow]s have emitted any values will be omitted.
 *
 * @param other2 Second [Flow]
 * @param other3 Third [Flow]
 * @param other4 Fourth [Flow]
 * @param other5 Fifth [Flow]
 * @param other6 Sixth [Flow]
 * @param other7 Seventh [Flow]
 * @param other8 Eighth [Flow]
 * @param other9 Ninth [Flow]
 * @param other10 Tenth [Flow]
 * @param transform A transform function to apply to each value from self combined with the latest values from the other [Flow]s, if any.
 */
public fun <A, B, C, D, E, F, G, H, I, J, R> Flow<A>.withLatestFrom(
  other2: Flow<B>,
  other3: Flow<C>,
  other4: Flow<D>,
  other5: Flow<E>,
  other6: Flow<F>,
  other7: Flow<G>,
  other8: Flow<H>,
  other9: Flow<I>,
  other10: Flow<J>,
  transform: suspend (A, B, C, D, E, F, G, H, I, J) -> R,
): Flow<R> {
  return withLatestFrom(arrayOf(other2, other3, other4, other5, other6, other7, other8, other9, other10)) { value, others ->
    transform(
      value,
      others[0] as B,
      others[1] as C,
      others[2] as D,
      others[3] as E,
      others[4] as F,
      others[5] as G,
      others[6] as H,
      others[7] as I,
      others[8] as J,
    )
  }
}
