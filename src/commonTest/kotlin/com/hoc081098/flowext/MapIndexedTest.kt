package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MapIndexedTest {
  @Test
  fun basic() = runTest {
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

  @Test
  fun upstreamError() = runTest {
    val throwable = RuntimeException()

    flow<Int> { throw throwable }
      .mapIndexed { index, value -> index to value }
      .test(listOf(Event.Error(throwable)))
  }

  @Test
  fun cancellation() = runTest {
    flow {
      repeat(5) {
        if (it == 2) throw CancellationException("")
        else emit(it)
      }
    }
      .mapIndexed { index, value -> index to value }
      .test(null) {
        assertEquals(
          listOf(
            Event.Value(0 to 0),
            Event.Value(1 to 1),
          ),
          it.take(2)
        )
        assertIs<CancellationException>(it.drop(2).single().errorOrThrow())
      }

    flowOf(0, 1, 2, 3, 4, 5, 6)
      .mapIndexed { index, value -> index to value }
      .take(2)
      .test(
        listOf(
          Event.Value(0 to 0),
          Event.Value(1 to 1),
          Event.Complete,
        )
      )
  }
}
