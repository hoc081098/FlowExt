package com.hoc081098.flowext

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

@ExperimentalTime
@ExperimentalCoroutinesApi
class TakeUntilTest {
  @Test
  fun sourceCompletesAfterNotifier() = suspendTest {
    range(0, 10)
      .onEach { delay(100) }
      .takeUntil(
        timer(
          Unit,
          Duration.milliseconds(350)
        )
      )
      .test {
        assertEquals(0, expectItem())
        assertEquals(1, expectItem())
        assertEquals(2, expectItem())
        expectComplete()
      }
  }

  @Test
  fun sourceCompletesBeforeNotifier() = suspendTest {
    range(0, 10)
      .onEach { delay(30) }
      .takeUntil(
        timer(
          Unit,
          Duration.seconds(10)
        )
      )
      .test {
        (0 until 10).forEach {
          assertEquals(it, expectItem())
        }
        expectComplete()
      }
  }

  @Test
  fun upstreamError() = suspendTest {
    flow<Nothing> { throw RuntimeException() }
      .takeUntil(timer(Unit, 100))
      .test { assertIs<RuntimeException>(expectError()) }

    flow {
      emit(1)
      throw RuntimeException()
    }
      .takeUntil(timer(Unit, 100))
      .test {
        assertEquals(1, expectItem())
        assertIs<RuntimeException>(expectError())
      }
  }

  @Test
  fun take() = suspendTest {
    flowOf(1, 2, 3)
      .takeUntil(timer(Unit, 100))
      .take(1)
      .test {
        assertEquals(1, expectItem())
        expectComplete()
      }
  }
}
