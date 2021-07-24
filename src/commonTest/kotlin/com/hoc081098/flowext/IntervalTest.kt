package com.hoc081098.flowext

import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take

@ExperimentalTime
class IntervalTest {
  @Test
  fun warm() = warmTest()

  @Test
  fun run() = suspendTest {
    interval(100, 200).take(20)
      .collect { println(it) }
  }
}
