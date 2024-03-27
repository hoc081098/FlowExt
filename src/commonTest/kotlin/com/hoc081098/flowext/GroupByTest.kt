/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Petrus Nguyễn Thái Học
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hoc081098.flowext

import com.hoc081098.flowext.utils.BaseTest
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy

private fun <T> Flow<T>.toListFlow(): Flow<List<T>> = flowFromSuspend { toList() }

@FlowExtPreview
@ExperimentalCoroutinesApi
class GroupByTest : BaseTest() {
  @Test
  fun basic() = runTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it.toListFlow() }
      .test(
        listOf(
          Event.Value(listOf(1, 3, 5, 7, 9)),
          Event.Value(listOf(2, 4, 6, 8, 10)),
          Event.Complete,
        ),
      )
  }

  @Test
  fun basicNullableValuesKeys() = runTest {
    val keys = linkedSetOf<Int?>()
    range(1, 10)
      .map { it.takeIf { it % 2 == 0 } }
      .groupBy { it }
      .flatMapMerge {
        keys += it.key
        it.toListFlow()
      }
      .test(
        listOf(
          Event.Value(listOf(null, null, null, null, null)),
          Event.Value(listOf(2)),
          Event.Value(listOf(4)),
          Event.Value(listOf(6)),
          Event.Value(listOf(8)),
          Event.Value(listOf(10)),
          Event.Complete,
        ),
      )

    assertEquals(actual = listOf(null, 2, 4, 6, 8, 10), expected = keys.toList())
  }

  @Test
  fun basicValueSelector() = runTest {
    range(1, 10)
      .groupBy(keySelector = { it % 2 }) { it + 1 }
      .flatMapMerge { it.toListFlow() }
      .test(
        listOf(
          Event.Value(listOf(2, 4, 6, 8, 10)),
          Event.Value(listOf(3, 5, 7, 9, 11)),
          Event.Complete,
        ),
      )
  }

  @Test
  fun single() = runTest {
    flowOf(1)
      .groupBy { it }
      .flatMapLatest { it.toListFlow() }
      .single()
      .single()
      .let { assertEquals(actual = it, expected = 1) }

    flowOf(1)
      .groupBy { null }
      .flatMapLatest { it.toListFlow() }
      .single()
      .single()
      .let { assertEquals(actual = it, expected = 1) }
  }

  @Test
  fun never() = runTest {
    var hasValue = false

    val job = neverFlow()
      .groupBy { it }
      .onEach { hasValue = true }
      .launchIn(this)
    advanceTimeBy(1000)
    job.cancel()

    assertFalse(hasValue)
  }

  @Test
  fun empty() = runTest {
    emptyFlow<Int>()
      .groupBy { it }
      .test(listOf(Event.Complete))
  }

  @Test
  fun failureUpstream() = runTest {
    assertFailsWith<TestException>(
      flow<Int> { throw TestException("Broken!") }
        .groupBy { it },
    )

    assertFailsWith<TestException>(
      flow<Int> { throw TestException("Broken!") }
        .groupBy { it }
        .flatMapMerge { it },
    )
  }

  @Test
  fun keySelectorThrows() = runTest {
    assertFailsWith<TestException>(
      flowOf(1)
        .groupBy { throw TestException("Broken!") }
        .groupBy { it },
    )
  }

  @Test
  fun valueSelectorThrows() = runTest {
    assertFailsWith<TestException>(
      flowOf(1)
        .groupBy(keySelector = { it }, valueSelector = { throw TestException("Broken!") })
        .groupBy { it },
    )
  }

  @Test
  fun oneOfEach() = runTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it.take(1) }
      .test(
        (1..10).map { Event.Value(it) } + Event.Complete,
      )
  }

  @Test
  fun maxGroups() = runTest {
    range(1, 10)
      .groupBy { it % 3 }
      .take(2)
      .flatMapMerge { it.toListFlow() }
      .test(
        listOf(
          Event.Value(listOf(1, 4, 7, 10)),
          Event.Value(listOf(2, 5, 8)),
          Event.Complete,
        ),
      )
  }

  @Test
  fun takeItems() = runTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it }
      .take(2)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Complete,
        ),
      )
  }

  @Test
  fun takeGroupsAndItems() = runTest {
    range(1, 10)
      .groupBy { it % 3 }
      .take(2)
      .flatMapMerge { it }
      .take(2)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Complete,
        ),
      )
  }

  @Test
  fun mainErrorsNoItems() = runTest {
    assertFailsWith<IllegalStateException>(
      (1..10)
        .asFlow()
        .map { if (it < 5) throw IllegalStateException("oops") else it }
        .groupBy { it % 2 == 0 }
        .flatMapMerge { it },
    )
  }

  @Test
  fun mainErrorsSomeItems() = runTest {
    assertFailsWith<IllegalStateException>(
      (1..10)
        .asFlow()
        .map { if (it > 5) throw IllegalStateException("oops") else it }
        .groupBy { it % 2 == 0 }
        .flatMapMerge { it },
    )
  }

  @Test
  fun groupByIdentity() = runTest {
    (1..10)
      .asFlow()
      .groupBy(keySelector = { it })
      .take(1)
      .flatMapMerge {
        it
          .onEach { delay(100) }
          .concatWith(timer(99, 100))
          .concatWith(
            neverFlow()
              .takeUntil(timer(Unit, 1000)),
          )
          .scan(emptyList<Int>()) { acc, e -> acc + e }
      }
      .test(
        listOf(
          Event.Value(emptyList()),
          Event.Value(listOf(1)),
          Event.Value(listOf(1, 99)),
          Event.Complete,
        ),
      )
  }

  @Test
  fun groupByFlatMapXXX() = runTest {
    val closeGroup = MutableSharedFlow<Unit>(extraBufferCapacity = Int.MAX_VALUE)

    flow {
      emit(1 to "1:a")
      delay(100)
      emit(1 to "1:b")
      delay(50)
      emit(1 to "1:c")

      emit(2 to "2:a")
      delay(100)
      emit(2 to "2:b")
      emit(2 to "2:c")
      emit(2 to "2:d")

      emit(3 to "3:a")
      delay(400)
      emit(3 to "3:b")

      closeGroup.emit(Unit)

      emit(1 to "1:a1")
      emit(1 to "1:b1")
      emit(1 to "1:c1")
    }
      .groupBy { it.first }
      .flatMapMerge { g ->
        g
          .takeUntil(closeGroup)
          .flatMapFirst { (_, v) -> timer(v, 300) }
      }
      .test(
        listOf(
          Event.Value("1:a"),
          Event.Value("2:a"),
          Event.Value("3:a"),
          Event.Value("3:b"),
          Event.Value("1:a1"),
          Event.Complete,
        ),
      )
  }
}
