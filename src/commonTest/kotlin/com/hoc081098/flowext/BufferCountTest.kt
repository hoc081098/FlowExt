package com.hoc081098.flowext

import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.collect

@ExperimentalTime
class BufferCountTest {
  @Test
  fun warm() = warmTest()

  @Test
  fun run() = suspendTest {
    range(0, 10).bufferCount(3).collect {
      println(it)
    }
  }
}
