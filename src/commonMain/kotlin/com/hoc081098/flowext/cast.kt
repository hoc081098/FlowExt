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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Adapt this [Flow] to be a `Flow<R>`.
 *
 * This Flow is wrapped as a `Flow<R>` which checks at run-time that each value event emitted
 * by this Flow is also an instance of R.
 *
 * At the collection time, if this [Flow] has any value that is not an instance of R,
 * a [ClassCastException] will be thrown.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <reified R> Flow<*>.cast(): Flow<R> = map { it as R }

/**
 * Adapt this `Flow<T?>` to be a `Flow<T>`.
 *
 * At the collection time, if this [Flow] has any `null` value,
 * a [NullPointerException] will be thrown.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <reified T : Any> Flow<T?>.castNotNull(): Flow<T> = map { it!! }

/**
 * Adapt this `Flow<T>` to be a `Flow<T?>`.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <T> Flow<T>.castNullable(): Flow<T?> = this

/**
 * Adapt this `Flow<*>` to be a `Flow<R?>`.
 * At the collection time, if this [Flow] has any value that is not an instance of R,
 * `null` will be emitted.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <reified R> Flow<*>.safeCast(): Flow<R?> = map { it as? R }
