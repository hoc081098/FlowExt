package com.hoc081098.flowext

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect

expect fun suspendTest(block: suspend CoroutineScope.() -> Unit)

fun unreached() {
  throw RuntimeException("Should not reach here!")
}

@ExperimentalTime
fun warmTest() = suspendTest {
  timer(
    Unit,
    Duration.milliseconds(1_500)
  ).collect()
}
