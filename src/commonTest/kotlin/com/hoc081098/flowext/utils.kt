package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlin.test.assertContentEquals

expect fun suspendTest(block: suspend CoroutineScope.() -> Unit)

suspend fun <T> Flow<T>.test(
  expected: List<Event<T>>?,
  expectation: (suspend (List<Event<T>>) -> Unit)? = null,
) {
  val events = materialize().toList()
  expected?.let { assertContentEquals(it, events) }
  expectation?.invoke(events)
}

fun warmTest() = suspendTest {
  delay(50)
  timer(Unit, 50).collect()
  delay(50)
  timer(Unit, 50).collect()
}
