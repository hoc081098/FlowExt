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

import com.hoc081098.flowext.internal.ClosedException
import com.hoc081098.flowext.internal.checkOwnership
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * Emits the values emitted by the source [Flow] until a [notifier] [Flow] emits a value or completes.
 *
 * @param notifier The [Flow] whose first emitted value or complete event
 * will cause the output [Flow] of [takeUntil] to stop emitting values from the source [Flow].
 */
public fun <T> Flow<T>.takeUntil(notifier: Flow<Any?>): Flow<T> = flow {
  val ownershipMarker = Any()

  try {
    coroutineScope {
      val job = launch(start = CoroutineStart.UNDISPATCHED) {
        notifier.take(1).collect()
        throw ClosedException(ownershipMarker)
      }

      collect { emit(it) }
      job.cancel()
    }
  } catch (e: ClosedException) {
    e.checkOwnership(owner = ownershipMarker)
  }
}
