/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

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
          Event.Complete
        )
      )
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
          Event.Complete
        )
      )
  }

  @Test
  fun oneOfEach() = runTest {
    range(1, 10)
      .groupBy { it % 2 }
      .flatMapMerge { it.take(1) }
      .test(
        (1..10).map { Event.Value(it) } + Event.Complete
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
          Event.Complete
        )
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
          Event.Complete
        )
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
          Event.Complete
        )
      )
  }

  @Test
  fun mainErrorsNoItems() = runTest {
    assertFailsWith<IllegalStateException>(
      (1..10)
        .asFlow()
        .map { if (it < 5) throw IllegalStateException("oops") else it }
        .groupBy { it % 2 == 0 }
        .flatMapMerge { it }
    )
  }

  @Test
  fun mainErrorsSomeItems() = runTest {
    assertFailsWith<IllegalStateException>(
      (1..10)
        .asFlow()
        .map { if (it > 5) throw IllegalStateException("oops") else it }
        .groupBy { it % 2 == 0 }
        .flatMapMerge { it }
    )
  }
}
