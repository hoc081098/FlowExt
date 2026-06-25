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
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

// TODO: Remove SynchronizedObject
@OptIn(InternalCoroutinesApi::class)
internal class SimpleLazy<T : Any>(
  initializer: () -> T,
) : SynchronizedObject() {
  private var _initializer: (() -> T)? = initializer

  @Volatile
  private var value: T? = null

  fun getValue(): T =
    value ?: synchronized(this) {
      value ?: _initializer!!().also {
        _initializer = null
        value = it
      }
    }

  fun getOrNull(): T? = value

  fun clear() {
    _initializer = null
    value = null
  }
}

internal fun <T : Any> simpleLazyOf(initializer: () -> T): SimpleLazy<T> =
  SimpleLazy(initializer)
