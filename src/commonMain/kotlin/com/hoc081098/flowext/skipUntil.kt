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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.selects.select

/**
 * TODO
 */
@FlowPreview
@ExperimentalCoroutinesApi
public fun <T> Flow<T>.skipUntil(notifier: Flow<Any?>): Flow<T> = flow {
  coroutineScope {
    val values = produceIn(this)
    val notifierChannel = produce<Nothing>() { notifier.take(1).collect() }

    var shouldEmit = false
    var loop = true

    while (loop) {
      ensureActive()

      select<Unit> {
        if (!shouldEmit) {
          notifierChannel.onReceiveCatching { result ->
            result.onFailure {
              it?.let { throw it }
              shouldEmit = true
            }
          }
        }

        values
          .onReceiveCatching { result ->
            result
              .onSuccess {
                if (shouldEmit) {
                  emit(it)
                }
              }
              .onFailure {
                it?.let { throw it }

                loop = false
                notifierChannel.cancel()
              }
          }
      }
    }
  }
}

/**
 * TODO
 */
@Suppress("NOTHING_TO_INLINE")
@FlowPreview
@ExperimentalCoroutinesApi
public inline fun <T> Flow<T>.dropUntil(notifier: Flow<Any?>): Flow<T> = skipUntil(notifier)
