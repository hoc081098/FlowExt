package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.take

class IntervalTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun run() = suspendTest {
    interval(100, 200)
      .take(20)
      .test((0 until 20).map { Event.Value(it) })
  }
}
