package com.hoc081098.flowext

import kotlin.test.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf

class ConcatTest {
  @Test
  fun run() = suspendTest {
    flowOf(1, 2, 3).concatWith(
      flowOf(4, 5, 6),
      flowOf(7, 8, 9)
    ).collect { println(it) }
  }
}
