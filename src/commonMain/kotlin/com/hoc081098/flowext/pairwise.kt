/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
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

import com.hoc081098.flowext.utils.NULL_VALUE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Groups pairs of consecutive emissions together and emits them as a pair.
 *
 * Emits the `(n)th` and `(n-1)th` events as a pair.
 * The first value won't be emitted until the second one arrives.
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
public fun <T> Flow<T>.pairwise(): Flow<Pair<T, T>> = flow {
  var last: Any? = null

  collect {
    if (last !== null) {
      emit(Pair(NULL_VALUE.unbox(last), it))
    }
    last = it ?: NULL_VALUE
  }
}
