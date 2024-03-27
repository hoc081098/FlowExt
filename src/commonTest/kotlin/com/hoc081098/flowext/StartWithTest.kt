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
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class StartWithTest : BaseTest() {
  @Test
  fun startWithSingleValue() = runTest {
    flowOf(2)
      .startWith(1)
      .test(listOf(Event.Value(1), Event.Value(2), Event.Complete))
  }

  @Test
  fun startWithValueFactory() = runTest {
    var i = 1
    var called = false
    val flow = flowOf(2).startWith {
      called = true
      i++
    }

    assertFalse(called)

    flow.test(listOf(Event.Value(1), Event.Value(2), Event.Complete))
    flow.test(listOf(Event.Value(2), Event.Value(2), Event.Complete))
    flow.test(listOf(Event.Value(3), Event.Value(2), Event.Complete))

    assertTrue(called)
  }

  @Test
  fun startWithTwoValues() = runTest {
    flowOf(3)
      .startWith(1, 2)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithThreeValues() = runTest {
    flowOf(4)
      .startWith(1, 2, 3)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithFourValues() = runTest {
    flowOf(5)
      .startWith(1, 2, 3, 4)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithFiveValues() = runTest {
    flowOf(6)
      .startWith(1, 2, 3, 4, 5)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithMoreFiveValues() = runTest {
    flowOf(7)
      .startWith(1, 2, 3, 4, 5, 6)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Complete,
        ),
      )

    flowOf(8)
      .startWith(1, 2, 3, 4, 5, 6, 7)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Value(8),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithIterable() = runTest {
    flowOf(7)
      .startWith(
        listOf(
          1,
          2,
          3,
          4,
          5,
          6,
        ),
      )
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Complete,
        ),
      )

    flowOf(8)
      .startWith(1, 2, 3, 4, 5, 6, 7)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Value(8),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithSequence() = runTest {
    flowOf(7)
      .startWith(
        sequenceOf(
          1,
          2,
          3,
          4,
          5,
          6,
        ),
      )
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Complete,
        ),
      )

    flowOf(8)
      .startWith(1, 2, 3, 4, 5, 6, 7)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Value(8),
          Event.Complete,
        ),
      )
  }

  @Test
  fun startWithFlow() = runTest {
    flowOf(7)
      .startWith(
        flow {
          repeat(6) {
            emit(it + 1)
          }
        },
      )
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Complete,
        ),
      )

    flowOf(8)
      .startWith(1, 2, 3, 4, 5, 6, 7)
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(4),
          Event.Value(5),
          Event.Value(6),
          Event.Value(7),
          Event.Value(8),
          Event.Complete,
        ),
      )
  }
}
