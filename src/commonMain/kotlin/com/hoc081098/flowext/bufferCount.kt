package com.hoc081098.flowext

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Buffers the source [Flow] values until the size hits the maximum [bufferSize] given.
 *
 * @param bufferSize The maximum size of the buffer emitted.
 * @param startBufferEvery Optional. Default is null.
 * Interval at which to start a new buffer.
 * For example if [startBufferEvery] is 2, then a new buffer will be started on every other value from the source.
 * A new buffer is started at the beginning of the source by default.
 */
@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
public fun <T> Flow<T>.bufferCount(
  bufferSize: Int,
  startBufferEvery: Int? = null,
): Flow<List<T>> {
  // If no `startBufferEvery` value was supplied, then we're
  // opening and closing on the bufferSize itself.
  return if (startBufferEvery == null || bufferSize == startBufferEvery) {
    bufferExact(bufferSize)
  } else {
    bufferSkip(bufferSize, startBufferEvery)
  }
}

@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
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

@InternalCoroutinesApi // TODO: Remove InternalCoroutinesApi (https://github.com/Kotlin/kotlinx.coroutines/issues/3078)
private fun <T> Flow<T>.bufferExact(bufferSize: Int): Flow<List<T>> {
  return flow {
    var buffer: MutableList<T> = mutableListOf()

    collect { element ->
      buffer += element

      if (buffer.size >= bufferSize) {
        emit(buffer)
        buffer = mutableListOf()
      }
    }

    // Emits remaining buffer
    if (buffer.isNotEmpty()) {
      emit(buffer)
    }
  }
}
