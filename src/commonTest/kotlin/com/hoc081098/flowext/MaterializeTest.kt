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
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MaterializeTest : BaseTest() {
  @Test
  fun testMaterialize_shouldMaterializeAHappyFlow() = runTest {
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
  fun testMaterialize_shouldMaterializeASadFlow() = runTest {
    val ex = TestException()

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
  fun testMaterialize_testCancellation() = runTest {
    val events1 = flowOf(1, 2, 3).take(1).materialize().toList()
    assertContentEquals(
      listOf(
        Event.Value(1),
        Event.Complete,
      ),
      events1,
    )

    val ex = TestException()
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
