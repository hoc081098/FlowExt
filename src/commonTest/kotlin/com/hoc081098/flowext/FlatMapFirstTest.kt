package com.hoc081098.flowext

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

@ExperimentalCoroutinesApi
class FlatMapFirstTest {
  @BeforeTest
  fun warm() = warmTest()

  @Test
  fun basic1() = suspendTest {
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
  fun basic2() = suspendTest {
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
  fun basic3() = suspendTest {
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
  fun testFailureUpstream() = suspendTest {
    val original = RuntimeException("Broken!")

    flow<Int> { throw original }
      .flatMapFirst { emptyFlow<Int>() }
      .test(listOf(Event.Error(original)))
  }

  @Test
  fun testFailureTransform() = suspendTest {
    val original = RuntimeException("Broken!")

    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        throw original
      } else {
        flowOf(v)
      }
    }.test(
      listOf(
        Event.Value(1),
        Event.Error(original),
      )
    )
  }
}
