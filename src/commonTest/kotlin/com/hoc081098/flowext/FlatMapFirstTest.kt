package com.hoc081098.flowext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class FlatMapFirstTest {
  @Test
  fun basic1() = runTest {
    flowOf("one", "two")
      .flatMapFirst { v ->
        flow {
          delay(100)
          emit(v)
        }
      }
      .test(
        listOf(
          Event.Value("one"),
          Event.Complete,
        )
      )
  }

  @Test
  fun basic2() = runTest {
    range(1, 10)
      .onEach { delay(140) }
      .flatMapFirst {
        range(it * 100, 5)
          .onEach { delay(42) }
      }
      .test(
        listOf(
          100, 101, 102, 103, 104,
          300, 301, 302, 303, 304,
          500, 501, 502, 503, 504,
          700, 701, 702, 703, 704,
          900, 901, 902, 903, 904,
        ).map { Event.Value(it) } + Event.Complete
      )
  }

  @Test
  fun basic3() = runTest {
    var input: Int? = null

    range(1, 10)
      .onEach { delay(140) }
      .flatMapFirst {
        input = it
        range(it * 100, 5).onEach { delay(42) }
      }
      .take(7)
      .test(
        listOf(
          100, 101, 102, 103, 104,
          300, 301
        ).map { Event.Value(it) } + Event.Complete
      )
    assertEquals(3, input)
  }

  @Test
  fun testFailureUpstream() = runTest {
    flow<Int> { throw RuntimeException("Broken!") }
      .flatMapFirst { emptyFlow<Int>() }
      .test(null) {
        assertIs<RuntimeException>(it.single().errorOrThrow())
      }
  }

  @Test
  fun testFailureTransform() = runTest {
    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        throw RuntimeException("Broken!")
      } else {
        flowOf(v)
      }
    }.test(null) { assertIs<RuntimeException>(it.single().errorOrThrow()) }

    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        yield()
        throw RuntimeException("Broken!")
      } else {
        flowOf(v)
      }
    }.test(null) {
      assertEquals(2, it.size)
      assertEquals(1, it[0].valueOrThrow())
      assertIs<RuntimeException>(it[1].errorOrThrow())
    }
  }

  @Test
  fun testFailureFlow() = runTest {
    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        flow { yield(); throw RuntimeException("Broken!") }
      } else {
        flowOf(v)
      }
    }.test(null) {
      assertEquals(2, it.size)
      assertEquals(1, it[0].valueOrThrow())
      assertIs<RuntimeException>(it[1].errorOrThrow())
    }

    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        flow { throw RuntimeException("Broken!") }
      } else {
        flowOf(v)
      }
    }.test(null) {
      assertEquals(2, it.size)
      assertEquals(1, it[0].valueOrThrow())
      assertIs<RuntimeException>(it[1].errorOrThrow())
    }
  }

  @Test
  fun testCancellation() = runTest {
    flow {
      repeat(5) {
        emit(
          flow {
            if (it == 2) throw CancellationException("")
            emit(1)
          }
        )
      }
    }
      .flattenFirst()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(1),
          Event.Value(1),
          Event.Value(1),
          Event.Complete,
        )
      )
  }
}
