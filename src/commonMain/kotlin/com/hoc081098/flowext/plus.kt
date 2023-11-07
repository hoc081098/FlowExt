/*
 * MIT License
 *
 * Copyright (c) 2023 Hoang Anh Chung
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

import kotlinx.coroutines.flow.*


/**
 * Combines two [Flow]s of the same base type [T] into a single [Flow] by concatenating their elements.
 * @param other The [Flow] to concatenate with the current [Flow].
 * @return A new [Flow] containing the concatenated elements of both the current and the [other] [Flow]s.
 *
 * Example:
 *
 * ``` kotlin
 *  val flow1 = flowOf(1, 2, 3)
 *  val flow2 = flowOf(4, 5, 6)
 *  val combinedFlow = flow1 + flow2 //1, 2, 3, 4, 5, 6
 * ```
 */

public operator fun <T, R : T> Flow<T>.plus(other: Flow<R>): Flow<T> = concat(this, other)

