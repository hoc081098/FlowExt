package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MapIndexedTest {
  @Test
  fun basic() = suspendTest {
    flowOf(1, 2, 3, 4)
      .mapIndexed { index, value -> index to value }
      .test(
        listOf(
          Event.Value(0 to 1),
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Value(3 to 4),
          Event.Complete,
        )
      )
  }
}
