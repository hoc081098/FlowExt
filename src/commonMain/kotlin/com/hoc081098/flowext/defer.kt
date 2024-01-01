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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Creates a [Flow] that, on collection, calls a [Flow] factory to make a [Flow] for each new [FlowCollector].
 *
 * In some circumstances, waiting until the last minute (that is, until collection time)
 * to generate the [Flow] can ensure that collectors receive the freshest data.
 *
 * Example of usage:
 *
 * ```
 * suspend fun remoteCall1(): R1 = ...
 * suspend fun remoteCall2(r1: R1): R2 = ...
 *
 * fun example1(): Flow<R2> = defer {
 *   val r1 = remoteCall1()
 *   val r2 = remoteCall2(r1)
 *   flowOf(r2)
 * }
 *
 * fun example2(): Flow<R1> = defer { flowOf(remoteCall1()) }
 * ```
 */
public fun <T> defer(flowFactory: suspend () -> Flow<T>): Flow<T> = flow { emitAll(flowFactory()) }
