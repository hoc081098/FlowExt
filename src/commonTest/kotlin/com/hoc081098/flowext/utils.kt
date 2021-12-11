package com.hoc081098.flowext

import kotlin.test.assertContentEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

suspend fun <T> Flow<T>.test(
  expected: List<Event<T>>?,
  expectation: (suspend (List<Event<T>>) -> Unit)? = null,
) {
  val events = materialize().toList()
  expected?.let { assertContentEquals(it, events) }
  expectation?.invoke(events)
}
