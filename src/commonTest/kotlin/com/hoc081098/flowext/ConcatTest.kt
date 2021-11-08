package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.flowOf

class ConcatTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun run() = suspendTest {
    flowOf(1, 2, 3)
      .concatWith(
        flowOf(4, 5, 6),
        flowOf(7, 8, 9)
      )
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Value(8),
          Event.Value(9),
          Event.Complete,
        ),
      )
  }
}
