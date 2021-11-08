package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test

class BufferCountTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun basic() = suspendTest {
    range(0, 10)
      .bufferCount(3)
      .test(
        listOf(
          Event.Value(listOf(0, 1, 2)),
          Event.Value(listOf(3, 4, 5)),
          Event.Value(listOf(6, 7, 8)),
          Event.Value(listOf(9)),
          Event.Complete,
        )
      )
  }
}
