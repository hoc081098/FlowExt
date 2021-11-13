package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MaterializeTest {
  @Test
  fun testMaterialize_shouldMaterializeAHappyFlow() = suspendTest {
    val events = flowOf(1, 2, 3).materialize().toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
      events,
    )

    assertEquals(Event.Complete, emptyFlow<Int>().materialize().single())
  }

  @Test
  fun testMaterialize_shouldMaterializeASadFlow() = suspendTest {
    val ex = RuntimeException()

    val events1 = flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .materialize()
      .toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Error(ex),
      ),
      events1,
    )

    val events2 = flowOf(1, 2, 3)
      .startWith(flow { throw ex })
      .materialize()
      .toList()
    assertContentEquals(
      listOf(Event.Error(ex)),
      events2,
    )

    val events3 = concat(
      flowOf(1, 2, 3),
      flow { throw ex },
      flowOf(4, 5, 6),
    )
      .materialize()
      .toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Error(ex),
      ),
      events3,
    )
  }

  @Test
  fun testMaterialize_testCancellation() = suspendTest {
    val events1 = flowOf(1, 2, 3).take(1).materialize().toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Complete,
      ),
      events1,
    )

    val ex = RuntimeException()
    val events2 = flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .take(3)
      .materialize()
      .toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Value(3),
        Event.Complete,
      ),
      events2,
    )

    val events3 = flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .take(2)
      .materialize()
      .toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Value(2),
        Event.Complete,
      ),
      events3,
    )
  }
}
