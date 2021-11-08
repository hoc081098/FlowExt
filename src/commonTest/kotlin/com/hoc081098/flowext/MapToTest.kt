package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

class MapToTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun basic() = suspendTest {
    val values = flowOf(1, 2, 3).mapTo(4).toList()
    assertEquals(values, listOf(4, 4, 4))
  }

  @Test
  fun upstreamError() = suspendTest {
    val throwable = RuntimeException()

    flow<Nothing> { throw throwable }
      .mapTo(2)
      .test(listOf(Event.Error(throwable)))
  }
}
