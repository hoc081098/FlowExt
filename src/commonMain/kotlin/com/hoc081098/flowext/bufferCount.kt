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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * This function is an alias to [bufferCount] operator.
 *
 * @see bufferCount
 */
public fun <T> Flow<T>.chunked(bufferSize: Int): Flow<List<T>> = bufferCount(bufferSize)

/**
 * Buffers the source [Flow] values until the size hits the maximum [bufferSize] given.
 *
 * Returns a [Flow] that emits buffers of items it collects from the current [Flow].
 * It emits connected, non-overlapping buffers, each containing [bufferSize] items.
 * When the current [Flow] completes, the resulting [Flow] emits the current buffer
 * and propagates the complete event from the current [Flow].
 * Note that if the current [Flow] throws an exception,
 * that exception is passed on immediately without first emitting the buffer it is in the process of assembling.
 *
 * @param bufferSize The maximum size of the buffer emitted.
 */
public fun <T> Flow<T>.bufferCount(bufferSize: Int): Flow<List<T>> = bufferExact(bufferSize)

/**
 * Buffers a number of values from the source [Flow] by [bufferSize]
 * then emits the buffer and clears it, and starts a new buffer each [startBufferEvery] values.
 *
 * When the current [Flow] completes, the resulting [Flow] emits active buffers
 * and propagates the complete event from the current [Flow].
 * Note that if the current [Flow] throws an exception,
 * that exception is passed on immediately without first emitting the buffer it is in the process of assembling.
 *
 * @param bufferSize The maximum size of the buffer emitted.
 * @param startBufferEvery Interval at which to start a new buffer.
 * For example if [startBufferEvery] is 2, then a new buffer will be started on every other value from the source.
 * A new buffer is started at the beginning of the source by default.
 */
public fun <T> Flow<T>.bufferCount(bufferSize: Int, startBufferEvery: Int): Flow<List<T>> {
  // If `startBufferEvery` is equals to `bufferSize`, then we're
  // opening and closing on the bufferSize itself.
  return if (startBufferEvery == bufferSize) {
    bufferExact(bufferSize)
  } else {
    bufferSkip(bufferSize, startBufferEvery)
  }
}

private fun <T> Flow<T>.bufferSkip(bufferSize: Int, skip: Int): Flow<List<T>> {
  return flow {
    val buffers = ArrayDeque<MutableList<T>>()
    var index = 0

    try {
      collect { element ->
        // Check to see if we need to start a buffer.
        // This will start one at the first value, and then
        // a new one every N after that.
        if (index++ % skip == 0) {
          buffers += mutableListOf<T>()
        }

        // Push our value into our active buffers.
        buffers.iterator().let { iterator ->
          iterator.forEach { buffer ->
            buffer += element

            // Check to see if we're over the bufferSize
            // if we are, record it so we can emit it later.
            // If we emitted it now and removed it, it would
            // mutate the `buffers` array while we're looping
            // over it.
            if (buffer.size >= bufferSize) {
              // We have found some buffers that are over the
              // `bufferSize`. Emit them, and remove them from our
              // buffers list.
              iterator.remove()
              emit(buffer.toList())
            }
          }
        }
      }

      // When the source completes, emit all of our
      // active buffers.
      buffers.forEach { emit(it.toList()) }
    } finally {
      buffers.clear()
    }
  }
}

private fun <T> Flow<T>.bufferExact(bufferSize: Int): Flow<List<T>> {
  return flow {
    var buffer: MutableList<T>? = null

    collect { element ->
      val b = buffer ?: mutableListOf<T>().also { buffer = it }
      b += element

      if (b.size >= bufferSize) {
        emit(b)
        buffer = null
      }
    }

    // Emits remaining buffer
    buffer?.let { emit(it) }
  }
}
