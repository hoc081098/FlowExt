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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.yield

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class FlatMapFirstTest : BaseTest() {
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
        ),
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
        ).map { Event.Value(it) } + Event.Complete,
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
          100,
          101,
          102,
          103,
          104,
          300,
          301,
        ).map { Event.Value(it) } + Event.Complete,
      )
    assertEquals(3, input)
  }

  @Test
  fun testFailureUpstream() = runTest {
    flow<Int> { throw TestException("Broken!") }
      .flatMapFirst { emptyFlow<Int>() }
      .test(null) {
        assertIs<TestException>(it.single().errorOrThrow())
      }
  }

  @Test
  fun testFailureTransform() = runTest {
    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        throw TestException("Broken!")
      } else {
        flowOf(v)
      }
    }.test(null) {
      assertEquals(2, it.size)
      assertEquals(1, it[0].valueOrThrow())
      assertIs<TestException>(it[1].errorOrThrow())
    }
  }

  @Test
  fun testFailureFlow() = runTest {
    flowOf(1, 2, 3).flatMapFirst { v ->
      if (v == 2) {
        flow { yield(); throw TestException("Broken!") }
      } else {
        flowOf(v)
      }
    }.test(null) {
      assertEquals(2, it.size)
      assertEquals(1, it[0].valueOrThrow())
      assertIs<TestException>(it[1].errorOrThrow())
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
          },
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
        ),
      )
  }
}
