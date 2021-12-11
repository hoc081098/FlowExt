package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class DematerializeTest {
  @Test
  fun testDematerialize_shouldDematerializeAHappyFlow() = runTest {
    flowOf(1, 2, 3)
      .materialize()
      .dematerialize()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )

    flowOf(
      Event.Value(1),
      Event.Value(2),
      Event.Value(3),
      Event.Complete,
    )
      .dematerialize()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )

    flowOf(
      Event.Value(1),
      Event.Value(2),
      Event.Value(3),
      Event.Complete,
      Event.Value(4),
      Event.Value(5),
      Event.Value(6),
    )
      .dematerialize()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )

    flowOf(Event.Complete).dematerialize().test(listOf(Event.Complete))
    emptyFlow<Event<Nothing>>().dematerialize().test(listOf(Event.Complete))
  }

  @Test
  fun testDematerialize_shouldDematerializeASadFlow() = runTest {
    val ex = RuntimeException()

    flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .materialize()
      .dematerialize()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Error(ex),
        )
      )

    flowOf(1, 2, 3)
      .startWith(flow { throw ex })
      .materialize()
      .dematerialize()
      .test(listOf(Event.Error(ex)))

    concat(
      flowOf(1, 2, 3),
      flow { throw ex },
      flowOf(4, 5, 6),
    )
      .materialize()
      .dematerialize()
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Error(ex),
        )
      )

    assertFailsWith<RuntimeException> { flowOf(Event.Error(ex)).dematerialize().collect() }
    assertFailsWith<RuntimeException> {
      flowOf(Event.Error(ex), Event.Value(1)).dematerialize().collect()
    }
    assertFailsWith<RuntimeException> {
      flowOf(Event.Error(ex), Event.Complete).dematerialize().collect()
    }
  }

  @Test
  fun testDematerialize_testCancellation() = runTest {
    flowOf(1, 2, 3)
      .materialize()
      .dematerialize()
      .take(1)
      .test(
        listOf(
          Event.Value(1),
          Event.Complete,
        )
      )

    val ex = RuntimeException()
    flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .materialize()
      .dematerialize()
      .take(3)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        )
      )

    flowOf(1, 2, 3)
      .concatWith(flow { throw ex })
      .materialize()
      .dematerialize()
      .take(2)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Complete,
        )
      )
  }
}
