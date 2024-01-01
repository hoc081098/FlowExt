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

package com.hoc081098.flowext.internal

import kotlin.concurrent.AtomicInt

internal actual class AtomicBoolean actual constructor(value: Boolean) {
  private val atomic = AtomicInt(value.asInt)

  actual fun compareAndSet(
    expect: Boolean,
    update: Boolean,
  ): Boolean = atomic.compareAndSet(expect.asInt, update.asInt)

  actual var value: Boolean
    get() = atomic.value.asBoolean
    set(value) {
      atomic.value = value.asInt
    }

  actual fun getAndSet(value: Boolean): Boolean = atomic.getAndSet(value.asInt).asBoolean
}

private inline val Boolean.asInt: Int get() = if (this) 1 else 0
private inline val Int.asBoolean: Boolean get() = this == 1
