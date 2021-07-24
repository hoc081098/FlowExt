package com.hoc081098.flowext

import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

expect fun suspendTest(block: suspend CoroutineScope.() -> Unit)

fun unreached() {
  throw RuntimeException("Should not reach here!")
}

@ExperimentalTime
fun warmTest() = suspendTest {
  assertEquals(1, 1)

  delay(100)
  timer(
    Unit,
    Duration.milliseconds(100)
  ).collect()

  assertEquals(2, 2)

  delay(300)
  timer(
    Unit,
    Duration.milliseconds(300)
  ).collect()
}
