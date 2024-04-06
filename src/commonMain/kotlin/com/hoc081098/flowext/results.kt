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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Maps values in the [Flow] to [successful results][Result.success],
 * and catches and wraps any exception into a [failure result][Result.failure].
 */
@FlowExtPreview
public fun <T> Flow<T>.mapToResult(): Flow<Result<T>> =
  map { Result.success(it) }
    .catchAndReturn { Result.failure(it) }

/**
 * Maps a [Flow] of [Result]s to a [Flow] of a mapped [Result]s.
 *
 * Any exception thrown by the [transform] function is caught,
 * and emitted as a [failure result][Result.failure] to the resulting flow.
 *
 * @see Result.mapCatching
 */
@FlowExtPreview
public fun <T, R> Flow<Result<T>>.mapResultCatching(transform: suspend (T) -> R): Flow<Result<R>> =
  map { result ->
    result
      .mapCatching { transform(it) }
      .onFailure {
        if (it is CancellationException) {
          throw it
        }
      }
  }

/**
 * Maps a [Flow] of [Result]s to a [Flow] of values from successful results.
 * Failure results are re-thrown as exceptions.
 *
 * @see Result.getOrThrow
 */
@FlowExtPreview
public fun <T> Flow<Result<T>>.throwFailure(): Flow<T> =
  map { it.getOrThrow() }
