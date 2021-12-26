package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertContentEquals

suspend fun <T> Flow<T>.test(
  expected: List<Event<T>>?,
  expectation: (suspend (List<Event<T>>) -> Unit)? = null,
) {
  val events = materialize().toList()
  expected?.let { assertContentEquals(it, events) }
  expectation?.invoke(events)
}

abstract class BaseTest {
  @ExperimentalCoroutinesApi
  protected fun runTest(testBody: suspend TestScope.() -> Unit): TestResult {
    return runTest(
      context = UnconfinedTestDispatcher(name = "${this::class.simpleName}-dispatchers"),
      testBody = testBody,
    )
  }
}
