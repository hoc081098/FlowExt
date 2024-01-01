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

import com.hoc081098.flowext.utils.BaseStepTest
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

@FlowPreview
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class SkipUntilTest : BaseStepTest() {
  @Test
  fun testSkipUntil() = runTest {
    // ----------1----------2----------3
    // ---------------|
    flowOf(1, 2, 3)
      .onEach { delay(100) }
      .skipUntil(timer(Unit, 150))
      .test(
        listOf(
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
      )

    flowOf(1, 2, 3)
      .onEach { delay(100) }
      .dropUntil(timer(Unit, 150))
      .test(
        listOf(
          Event.Value(2),
          Event.Value(3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testSkipUntilNever() = runTest {
    flowOf(1, 2, 3, 4)
      .skipUntil(neverFlow())
      .test(listOf(Event.Complete))
  }

  @Test
  fun testSkipUntilEmpty() = runTest {
    flowOf(1, 2, 3, 4)
      .skipUntil(emptyFlow())
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
  fun testSkipUntilFailureUpstream() = runTest {
    // 01--------------------2X
    // ----------100

    val source = flow {
      expect(2)
      emit(0)
      expect(3)
      emit(1)

      delay(20)
      expect(5)

      emit(2)
      expect(7)
      throw TestException()
    }

    val notifier = flowOf(100).onEach {
      delay(10)
      expect(4)
    }

    expect(1)
    assertFailsWith<TestException>(
      source
        .skipUntil(notifier)
        .onEach {
          assertEquals(2, it)
          expect(6)
        },
    )
    finish(8)
  }

  @Test
  fun testSkipUntilCancellation() = runTest {
    flow {
      emit(0)
      delay(200)
      emit(1)
      emit(2)
      emit(3)
      expectUnreached() // Cancelled by take
      emit(5)
    }.skipUntil(timer(Unit, 100))
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
  fun testSkipUntilNotifierFailure() = runTest {
    flow {
      emit(0)
      delay(200)
      emit(1)
      emit(2)
      emit(3)
    }.skipUntil(
      timer(Unit, 100).onEach {
        throw TestException()
      },
    )
      .let {
        it.test(null) { events ->
          assertEquals(1, events.size)
        }
        assertFailsWith<TestException>(it)
      }
  }
}
