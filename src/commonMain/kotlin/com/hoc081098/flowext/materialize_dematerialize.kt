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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * Represents all of the notifications from the source [Flow] as `value` emissions marked with their original types within [Event] objects.
 */
public fun <T> Flow<T>.materialize(): Flow<Event<T>> = map<T, Event<T>> { Event.Value(it) }
  .onCompletion { if (it === null) emit(Event.Complete) }
  .catch { emit(Event.Error(it)) }

/**
 * Converts a [Flow] of [Event] objects into the emissions that they represent.
 */
public fun <T> Flow<Event<T>>.dematerialize(): Flow<T> = flow {
  val ownershipMarker = Any()

  try {
    collect {
      when (it) {
        Event.Complete -> throw ClosedException(ownershipMarker)
        is Event.Error -> throw it.error
        is Event.Value -> emit(it.value)
      }
    }
  } catch (e: ClosedException) {
    e.checkOwnership(owner = ownershipMarker)
  }
}
