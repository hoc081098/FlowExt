package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

public fun <T> Flow<T>.bufferCount(
  bufferSize: Int,
  startBufferEvery: Int? = null,
): Flow<List<T>> {
  return flow {
    val buffer = mutableListOf<T>()

    collect { element ->
      if (buffer.size < bufferSize) buffer += element

      if (buffer.size == bufferSize) {
        emit(buffer.toList())
        buffer.clear()
      }
    }

    if (buffer.isNotEmpty()) {
      emit(buffer.toList())
      buffer.clear()
    }
  }
}
