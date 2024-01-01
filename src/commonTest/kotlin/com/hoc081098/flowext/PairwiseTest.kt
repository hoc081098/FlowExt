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
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take

private data class MyTuple2<A, B>(val first: A, val second: B)

@Suppress("NOTHING_TO_INLINE")
private inline infix fun <A, B> A.with(second: B) = MyTuple2(this, second)

@ExperimentalCoroutinesApi
class PairwiseTest : BaseStepTest() {
  @Test
  fun testPairwise() = runTest {
    range(0, 4)
      .pairwise()
      .test(
        listOf(
          Event.Value(0 to 1),
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Complete,
        ),
      )

    range(0, 4)
      .bufferCount(bufferSize = 2, startBufferEvery = 1)
      .mapNotNull {
        if (it.size < 2) {
          null
        } else {
          it[0] to it[1]
        }
      }
      .test(
        listOf(
          Event.Value(0 to 1),
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Complete,
        ),
      )

    range(0, 4)
      .zipWithNext()
      .test(
        listOf(
          Event.Value(0 to 1),
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseNullable() = runTest {
    // 0 - null - 2 - null

    range(0, 4)
      .map { it.takeIf { it % 2 == 0 } }
      .pairwise()
      .test(
        listOf(
          Event.Value(0 to null),
          Event.Value(null to 2),
          Event.Value(2 to null),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseEmpty() = runTest {
    emptyFlow<Int>()
      .pairwise()
      .test(
        listOf(
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseSingle() = runTest {
    flowOf(1)
      .pairwise()
      .test(
        listOf(
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseFailureUpstream() = runTest {
    assertFailsWith<TestException>(
      flow<Int> { throw TestException() }
        .pairwise(),
    )
  }

  @Test
  fun testPairwiseCancellation() = runTest {
    range(1, 100)
      .pairwise()
      .take(2)
      .test(
        listOf(
          Event.Value(1 to 2),
          Event.Value(2 to 3),
          Event.Complete,
        ),
      )
  }
}

@ExperimentalCoroutinesApi
class PairwiseWithTransformTest : BaseStepTest() {
  @Test
  fun testPairwise() = runTest {
    range(0, 4)
      .pairwise(::MyTuple2)
      .also { assertIs<Flow<MyTuple2<Int, Int>>>(it) }
      .test(
        listOf(
          Event.Value(0 with 1),
          Event.Value(1 with 2),
          Event.Value(2 with 3),
          Event.Complete,
        ),
      )

    range(0, 4)
      .bufferCount(bufferSize = 2, startBufferEvery = 1)
      .mapNotNull {
        if (it.size < 2) {
          null
        } else {
          it[0] with it[1]
        }
      }
      .also { assertIs<Flow<MyTuple2<Int, Int>>>(it) }
      .test(
        listOf(
          Event.Value(0 with 1),
          Event.Value(1 with 2),
          Event.Value(2 with 3),
          Event.Complete,
        ),
      )

    range(0, 4)
      .zipWithNext(::MyTuple2)
      .also { assertIs<Flow<MyTuple2<Int, Int>>>(it) }
      .test(
        listOf(
          Event.Value(0 with 1),
          Event.Value(1 with 2),
          Event.Value(2 with 3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseWithSuspend() = runTest {
    range(0, 4)
      .pairwise { a, b ->
        delay(100)
        MyTuple2(a, b)
      }
      .also { assertIs<Flow<MyTuple2<Int, Int>>>(it) }
      .test(
        listOf(
          Event.Value(0 with 1),
          Event.Value(1 with 2),
          Event.Value(2 with 3),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseNullable() = runTest {
    // 0 - null - 2 - null

    range(0, 4)
      .map { it.takeIf { it % 2 == 0 } }
      .pairwise(::MyTuple2)
      .test(
        listOf(
          Event.Value(0 with null),
          Event.Value(null with 2),
          Event.Value(2 with null),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseEmpty() = runTest {
    emptyFlow<Int>()
      .pairwise(::MyTuple2)
      .test(
        listOf(
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseSingle() = runTest {
    flowOf(1)
      .pairwise(::MyTuple2)
      .test(
        listOf(
          Event.Complete,
        ),
      )
  }

  @Test
  fun testPairwiseFailureUpstream() = runTest {
    assertFailsWith<TestException>(
      flow<Int> { throw TestException() }
        .pairwise(::MyTuple2),
    )
  }

  @Test
  fun testPairwiseFailureTransform() = runTest {
    // empty flow will not call transform function
    emptyFlow<Int>()
      .pairwise<_, String> { _, _ -> throw TestException("Broken!") }
      .test(
        listOf(
          Event.Complete,
        ),
      )

    // single element flow will not call transform function
    flowOf(1)
      .pairwise<_, String> { _, _ -> throw TestException("Broken!") }
      .test(
        listOf(
          Event.Complete,
        ),
      )

    // propagate exception from transform function
    assertFailsWith<TestException>(
      flowOf(1, 2)
        .pairwise<_, String> { _, _ -> throw TestException("Broken!") },
    )
  }

  @Test
  fun testPairwiseCancellation() = runTest {
    range(1, 100)
      .pairwise(::MyTuple2)
      .take(2)
      .test(
        listOf(
          Event.Value(1 with 2),
          Event.Value(2 with 3),
          Event.Complete,
        ),
      )
  }
}
