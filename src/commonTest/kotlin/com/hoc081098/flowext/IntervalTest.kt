package com.hoc081098.flowext

import kotlin.test.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take

class IntervalTest {
  @Test
  fun run() = suspendTest {
    interval(100, 200).take(20)
      .collect { println(it) }
  }
}
