package com.hoc081098.flowext

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RangeTest {
  @Test
  fun empty() = suspendTest {
    range(0, 0).test { expectComplete() }

    range(0, -2).test { expectComplete() }
  }

  @Test
  fun emitsRangeOfIntegers() = suspendTest {
    range(0, 5).test {
      assertEquals(0, expectItem())
      assertEquals(1, expectItem())
      assertEquals(2, expectItem())
      assertEquals(3, expectItem())
      assertEquals(4, expectItem())
      expectComplete()
    }
  }
}
