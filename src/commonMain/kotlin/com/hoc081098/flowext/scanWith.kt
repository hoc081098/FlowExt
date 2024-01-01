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

import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan

/**
 * Folds the given flow with [operation], emitting every intermediate result,
 * including the initial value supplied by [initialSupplier] at the collection time.
 *
 * Note that the returned initial value should be immutable (or should not be mutated)
 * as it is shared between different collectors.
 *
 * This is a variant of [scan] that the initial value is lazily supplied,
 * which is useful when the initial value is expensive to create
 * or depends on a logic that should be executed at the collection time (lazy semantics).
 *
 * For example:
 * ```kotlin
 * flowOf(1, 2, 3)
 *     .scanWith({ emptyList<Int>() }) { acc, value -> acc + value }
 *     .toList()
 * ```
 * will produce `[[], [1], [1, 2], [1, 2, 3]]`.
 *
 * Another example:
 * ```kotlin
 * // Logic to calculate initial value (e.g. call API, read from DB, etc.)
 * suspend fun calculateInitialValue(): Int {
 *   println("calculateInitialValue")
 *   delay(1000)
 *   return 0
 * }
 *
 * flowOf(1, 2, 3).scanWith(::calculateInitialValue) { acc, value -> acc + value }
 * ```
 *
 * @param initialSupplier a function that returns the initial (seed) accumulator value for each individual collector.
 * @param operation an accumulator function to be invoked on each item emitted by the current [Flow],
 * whose result will be emitted to collector via [FlowCollector.emit]
 * and used in the next accumulator call.
 */
@OptIn(ExperimentalTypeInference::class)
public fun <T, R> Flow<T>.scanWith(
  initialSupplier: suspend () -> R,
  @BuilderInference operation: suspend (accumulator: R, value: T) -> R,
): Flow<R> =
  // inline [defer] here to avoid unnecessary allocation
  flow { return@flow emitAll(scan(initialSupplier(), operation)) }
