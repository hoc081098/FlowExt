package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class WithLatestFromTest {
  @Test
  fun basic() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to 4,
        "b" to 4,
        "c" to 4,
        "d" to 4,
        "e" to 4,
      )
    )
  }

  @Test
  fun basicWithNull() = runTest {
    val f1 = flowOf(1, 2, 3, 4, null)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to null,
        "b" to null,
        "c" to null,
        "d" to null,
        "e" to null,
      )
    )
  }

  @Test
  fun basic2() = runTest {
    val f1 = flowOf(1, 2, 3, 4).onEach { delay(300) }
    val f2 = flowOf("a", "b", "c", "d", "e").onEach { delay(100) }
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "c" to 1,
        "d" to 1,
        "e" to 1,
      )
    )
  }

  @Test
  fun testWithLatestFrom_failureUpStream() = runTest {
    assertFailsWith<RuntimeException> {
      flow<Int> { throw RuntimeException() }
        .withLatestFrom(neverFlow())
        .collect()
    }

    assertFailsWith<RuntimeException> {
      neverFlow()
        .withLatestFrom(flow<Int> { throw RuntimeException() })
        .collect()
    }
  }

  @Test
  fun testWithLatestFrom_cancellation() = runTest {
    assertFailsWith<CancellationException> {
      flow {
        emit(1)
        throw CancellationException("")
      }
        .withLatestFrom(emptyFlow<Nothing>())
        .collect()
    }

    flowOf(1)
      .withLatestFrom(
        flow {
          emit(2)
          throw CancellationException("")
        }
      )
      .test(
        listOf(
          Event.Value(1 to 2),
          Event.Complete
        )
      )
  }
}
