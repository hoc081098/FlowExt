package com.hoc081098.flowext

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

@ExperimentalTime
class MapToTest {
  @Test
  fun basic() = suspendTest {
    val values = flowOf(1, 2, 3).mapTo(4).toList()
    assertEquals(values, listOf(4, 4, 4))
  }

  @Test
  fun upstreamError() = suspendTest {
    flow<Nothing> { throw RuntimeException() }
      .mapTo(2)
      .test { assertIs<RuntimeException>(expectError()) }
  }
}
