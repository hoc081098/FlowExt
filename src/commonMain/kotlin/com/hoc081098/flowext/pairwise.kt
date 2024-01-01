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

import com.hoc081098.flowext.internal.INTERNAL_NULL_VALUE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ------------------------------------------- PAIRWISE -------------------------------------------

/**
 * Groups pairs of consecutive emissions together and emits them as a pair.
 *
 * Emits the `(n)th` and `(n-1)th` events as a pair.
 * The first value won't be emitted until the second one arrives.
 * The resulting [Flow] is empty if this [Flow] emits less than two elements.
 *
 * This operator is more optimizer than [bufferCount] version:
 * ```kotlin
 * val flow: Flow<T>
 *
 * val result: Flow<Pair<T, T>> = flow
 *   .bufferCount(bufferSize = 2, startBufferEvery = 1)
 *   .mapNotNull {
 *     if (it.size < 2) null
 *     else it[0] to it[1]
 *   }
 * ```
 */
public fun <T> Flow<T>.pairwise(): Flow<Pair<T, T>> = pairwiseInternal(::Pair)

/**
 * Groups pairs of consecutive emissions together and emits the result of applying [transform]
 * function to each pair.
 *
 * The first value won't be emitted until the second one arrives.
 * The resulting [Flow] is empty if this [Flow] emits less than two elements.
 *
 * This operator is more optimizer than [bufferCount] version:
 * ```kotlin
 * val flow: Flow<T>
 *
 * val result: Flow<R> = flow
 *   .bufferCount(bufferSize = 2, startBufferEvery = 1)
 *   .mapNotNull {
 *     if (it.size < 2) null
 *     else transform(it[0], it[1])
 *   }
 * ```
 *
 * @param transform A function to apply to each pair of consecutive emissions.
 */
public fun <T, R> Flow<T>.pairwise(transform: suspend (a: T, b: T) -> R): Flow<R> =
  pairwiseInternal(transform)

// ----------------------------------------- ZIP WITH NEXT -----------------------------------------

/**
 * This function is an alias to [pairwise] operator.
 *
 * Groups pairs of consecutive emissions together and emits them as a pair.
 *
 * @see pairwise
 */
public fun <T> Flow<T>.zipWithNext(): Flow<Pair<T, T>> = pairwise()

/**
 * This function is an alias to [pairwise] operator.
 *
 * Groups pairs of consecutive emissions together and emits the result of applying [transform]
 * function to each pair.
 *
 * @see pairwise
 */
public fun <T, R> Flow<T>.zipWithNext(transform: suspend (a: T, b: T) -> R): Flow<R> =
  pairwise(transform)

// ------------------------------------------- INTERNAL -------------------------------------------

private fun <T, R> Flow<T>.pairwiseInternal(transform: suspend (a: T, b: T) -> R): Flow<R> = flow {
  var last: Any? = INTERNAL_NULL_VALUE

  collect {
    if (last !== INTERNAL_NULL_VALUE) {
      @Suppress("UNCHECKED_CAST")
      emit(transform(last as T, it))
    }
    last = it
  }
}
