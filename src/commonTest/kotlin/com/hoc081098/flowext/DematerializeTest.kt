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
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class DematerializeTest : BaseTest() {
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
        ),
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
        ),
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
        ),
      )

    flowOf(Event.Complete).dematerialize().test(listOf(Event.Complete))
    emptyFlow<Event<Nothing>>().dematerialize().test(listOf(Event.Complete))
  }

  @Test
  fun testDematerialize_shouldDematerializeASadFlow() = runTest {
    val ex = TestException()

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
        ),
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
        ),
      )

    assertFailsWith<TestException> { flowOf(Event.Error(ex)).dematerialize().collect() }
    assertFailsWith<TestException> {
      flowOf(Event.Error(ex), Event.Value(1)).dematerialize().collect()
    }
    assertFailsWith<TestException> {
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
        ),
      )

    val ex = TestException()
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
        ),
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
        ),
      )
  }
}
