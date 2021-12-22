package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class RangeTest {
  @Test
  fun empty() = runTest {
    range(0, 0).test(listOf(Event.Complete))
    range(0, -2).test(listOf(Event.Complete))
  }

  @Test
  fun emitsRangeOfIntegers() = runTest {
    range(0, 5).test(
      listOf(
        Event.Value(0),
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Value(4),
        Event.Complete,
      )
    )
  }
}
