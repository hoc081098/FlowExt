package com.hoc081098.flowext

import kotlin.test.Test
import kotlinx.coroutines.flow.collect

class BufferCountTest {
  @Test
  fun run() = suspendTest {
    range(0, 10).bufferCount(3).collect {
      println(it)
    }
  }
}
