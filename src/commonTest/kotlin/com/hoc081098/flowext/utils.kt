package com.hoc081098.flowext

import com.hoc081098.flowext.kotlinx_coroutines_test.TestCoroutineDispatcher
import com.hoc081098.flowext.kotlinx_coroutines_test.runBlockingTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlin.test.assertContentEquals

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
fun suspendTest(block: suspend CoroutineScope.() -> Unit) {
  val testCoroutineDispatcher = TestCoroutineDispatcher()
  testCoroutineDispatcher.runBlockingTest(block)
  testCoroutineDispatcher.cleanupTestCoroutines()
}

suspend fun <T> Flow<T>.test(
  expected: List<Event<T>>?,
  expectation: (suspend (List<Event<T>>) -> Unit)? = null,
) {
  val events = materialize().toList()
  expected?.let { assertContentEquals(it, events) }
  expectation?.invoke(events)
}
