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

import kotlin.native.ref.createCleaner
import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.posix.PTHREAD_MUTEX_RECURSIVE
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_mutexattr_destroy
import platform.posix.pthread_mutexattr_init
import platform.posix.pthread_mutexattr_settype
import platform.posix.pthread_mutexattr_t

@OptIn(
  kotlin.experimental.ExperimentalNativeApi::class,
  kotlinx.cinterop.ExperimentalForeignApi::class,
)
internal actual class Lock actual constructor() {
  private val resources = Resources()

  @Suppress("unused")
  // The returned Cleaner must be assigned to a property
  @ExperimentalStdlibApi
  private val cleaner = createCleaner(resources, Resources::destroy)

  actual inline fun <T> synchronizedImpl(block: () -> T): T {
    lock()
    return try {
      block()
    } finally {
      unlock()
    }
  }

  fun lock() {
    pthread_mutex_lock(resources.mutex.ptr)
  }

  fun unlock() {
    pthread_mutex_unlock(resources.mutex.ptr)
  }

  private class Resources {
    private val arena = Arena()
    private val attr: pthread_mutexattr_t = arena.alloc()
    val mutex: pthread_mutex_t = arena.alloc()

    init {
      pthread_mutexattr_init(attr.ptr)
      pthread_mutexattr_settype(attr.ptr, PTHREAD_MUTEX_RECURSIVE)
      pthread_mutex_init(mutex.ptr, attr.ptr)
    }

    fun destroy() {
      pthread_mutex_destroy(mutex.ptr)
      pthread_mutexattr_destroy(attr.ptr)
      arena.clear()
    }
  }
}
