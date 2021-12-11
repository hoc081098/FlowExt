package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MapToTest {
  @Test
  fun basic() = runTest {
    val values = flowOf(1, 2, 3).mapTo(4).toList()
    assertEquals(values, listOf(4, 4, 4))
  }

  @Test
  fun upstreamError() = runTest {
    val throwable = RuntimeException()

    flow<Nothing> { throw throwable }
      .mapTo(2)
      .test(listOf(Event.Error(throwable)))
  }
}
