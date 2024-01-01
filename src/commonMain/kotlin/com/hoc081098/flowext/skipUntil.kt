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

import com.hoc081098.flowext.internal.AtomicBoolean
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * Returns a [Flow] that skips items emitted by the source [Flow] until a second [Flow] emits a value or completes.
 *
 * @param notifier The second [Flow] that has to emit a value before the source [Flow]'s values
 * begin to be mirrored by the resulting [Flow].
 */
public fun <T> Flow<T>.skipUntil(notifier: Flow<Any?>): Flow<T> = flow {
  coroutineScope {
    val shouldEmit = AtomicBoolean(false)

    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      notifier.take(1).collect()
      shouldEmit.value = true
    }

    collect {
      if (shouldEmit.value) {
        emit(it)
      }
    }

    job.cancel()
  }
}

/**
 * This function is an alias to [skipUntil] operator.
 *
 * @see skipUntil
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <T> Flow<T>.dropUntil(notifier: Flow<Any?>): Flow<T> = skipUntil(notifier)
