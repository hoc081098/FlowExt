package com.hoc081098.flowext

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

@FlowPreview
class GroupByTest {
  @Test
  fun basic() = suspendTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge {
        flow { emit(it.toList()) }
      }
      .assertResultSet(listOf(1, 3, 5, 7, 9), listOf(2, 4, 6, 8, 10))
  }

  @Test
  fun basicValueSelector() = suspendTest {
    range(1, 10)
      .groupBy({ it % 2 }) { it + 1 }
      .flatMapMerge {
        flow { emit(it.toList()) }
      }
      .assertResultSet(listOf(2, 4, 6, 8, 10), listOf(3, 5, 7, 9, 11))
  }

  @Test
  fun oneOfEach() = suspendTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it.take(1) }
      .toList()
      .let { println(it) }
  }

  @Test
  fun maxGroups() = suspendTest {
    range(1, 10)
      .groupBy { it % 3 }
      .take(2)
      .flatMapMerge { it.take(1) }
      .assertResultSet(listOf(1, 4, 7, 10), listOf(2, 5, 8))
  }

  @Test
  fun takeItems() = suspendTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it }
      .take(2)
      .assertResultSet(1, 2)
  }

  @Test
  fun takeGroupsAndItems() = suspendTest {
    range(1, 10)
      .groupBy { it % 3 }
      .take(2)
      .flatMapMerge { it }
      .take(2)
      .toList()
  }
}

suspend fun <T> Flow<T>.assertResultSet(vararg values: T) {
  val set = HashSet<T>()

  collect {
    set.add(it)
  }

  assertEquals(values.size, set.size, "Number of values differ")

  values.forEach { assertTrue(set.contains(it), "Missing: $it") }
}
