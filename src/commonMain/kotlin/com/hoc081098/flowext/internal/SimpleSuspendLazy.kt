/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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

@file:Suppress("ktlint:standard:property-naming")

package com.hoc081098.flowext.internal

import kotlin.concurrent.Volatile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SimpleSuspendLazy<T : Any>(
  initializer: suspend () -> T,
) {
  private val mutex = Mutex()

  @Volatile
  private var _initializer: (suspend () -> T)? = initializer

  @Volatile
  private var value: T? = null

  suspend fun getValue(): T =
    value ?: mutex.withLock {
      value ?: _initializer!!().also {
        _initializer = null
        value = it
      }
    }

  fun clear() {
    _initializer = null
    value = null
  }
}

internal fun <T : Any> simpleSuspendLazy(initializer: suspend () -> T): SimpleSuspendLazy<T> =
  SimpleSuspendLazy(initializer)
