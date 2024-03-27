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

internal expect class AtomicRef<T>(initialValue: T) {
  var value: T

  fun compareAndSet(expect: T, update: T): Boolean
}

// Copy from: https://github.com/arrow-kt/arrow/blob/6fb6a75b131f5bbb272611bf277e263ff791cb67/arrow-libs/core/arrow-atomic/src/commonMain/kotlin/arrow/atomic/Atomic.kt#L44

/**
 * Infinite loop that reads this atomic variable and performs the specified [action] on its value.
 */
internal inline fun <T> AtomicRef<T>.loop(action: (T) -> Unit): Nothing {
  while (true) {
    action(value)
  }
}

internal fun <T> AtomicRef<T>.tryUpdate(function: (T) -> T): Boolean {
  val cur = value
  val upd = function(cur)
  return compareAndSet(cur, upd)
}

internal inline fun <T> AtomicRef<T>.update(function: (T) -> T) {
  while (true) {
    val cur = value
    val upd = function(cur)
    if (compareAndSet(cur, upd)) return
  }
}

/**
 * Updates variable atomically using the specified [function] of its value and returns its old value.
 */
internal inline fun <T> AtomicRef<T>.getAndUpdate(function: (T) -> T): T {
  while (true) {
    val cur = value
    val upd = function(cur)
    if (compareAndSet(cur, upd)) return cur
  }
}

/**
 * Updates variable atomically using the specified [function] of its value and returns its new value.
 */
internal inline fun <T> AtomicRef<T>.updateAndGet(function: (T) -> T): T {
  while (true) {
    val cur = value
    val upd = function(cur)
    if (compareAndSet(cur, upd)) return upd
  }
}
