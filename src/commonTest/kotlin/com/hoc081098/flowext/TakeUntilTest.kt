package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@InternalCoroutinesApi
@ExperimentalTime
@ExperimentalCoroutinesApi
class TakeUntilTest {
  @Test
  fun takeUntilSingle() = suspendTest {
    range(0, 10)
      .takeUntil(flowOf(1))
      .test(listOf(Event.Complete))

    flowOf(1)
      .takeUntil(flowOf(1))
      .test(listOf(Event.Complete))
  }

  @Test
  fun sourceCompletesAfterNotifier() = suspendTest {
    range(0, 10)
      .onEach { delay(100) }
      .onCompletion { println(it) }
      .takeUntil(
        timer(
          Unit,
          Duration.milliseconds(470)
        )
      )
      .test(
        listOf(
          Event.Value(0),
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )
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
      .test(
        (0 until 10).map { Event.Value(it) } +
          Event.Complete
      )
  }

  @Test
  fun upstreamError() = suspendTest {
    flow<Nothing> { throw RuntimeException() }
      .takeUntil(timer(Unit, 100))
      .test(null) {
        assertIs<RuntimeException>(it.single().throwableOrThrow())
      }

    flow {
      emit(1)
      throw RuntimeException()
    }
      .takeUntil(timer(Unit, 100))
      .test(null) {
        assertEquals(2, it.size)
        assertEquals(1, it[0].valueOrThrow())
        assertIs<RuntimeException>(it[1].throwableOrThrow())
      }
  }

  @Test
  fun take() = suspendTest {
    flowOf(1, 2, 3)
      .takeUntil(timer(Unit, 100))
      .take(1)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        )
      )
  }
}
