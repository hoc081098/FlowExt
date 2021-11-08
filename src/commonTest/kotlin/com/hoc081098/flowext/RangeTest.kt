package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test

class RangeTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun empty() = suspendTest {
    range(0, 0).test(listOf(Event.Complete))
    range(0, -2).test(listOf(Event.Complete))
  }

  @Test
  fun emitsRangeOfIntegers() = suspendTest {
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
