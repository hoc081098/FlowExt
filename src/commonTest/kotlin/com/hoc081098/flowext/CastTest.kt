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
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
class CastTest : BaseTest() {
  @Test
  fun testCastSuccess() = runTest {
    assertIs<Flow<String>>(flowOf(1, 2, 3).cast<String>())

    assertIs<Flow<Int>>(
      flowOf<Any?>(1, 2, 3)
        .cast<Int>(),
    )
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
  fun testCastFailure() = runTest {
    assertFailsWith<ClassCastException>(
      flowOf(1, 2, 3).cast<String>(),
    )
  }

  @Test
  fun testCastNotNullSuccess() = runTest {
    assertIs<Flow<Int>>(
      flowOf(1, 2, 3, null).castNotNull(),
    )

    assertIs<Flow<Int>>(
      flowOf<Int?>(1, 2, 3)
        .castNotNull(),
    )
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
  fun testCastNotNullFailure() = runTest {
    assertFailsWith<NullPointerException>(
      flowOf(1, 2, 3, null).castNotNull(),
    )
  }

  @Test
  fun testCastNullable() {
    assertIs<Flow<Int?>>(flowOf(1, 2, 3).castNullable())
    assertIs<Flow<Int?>>(flowOf(1, 2, 3, null).castNullable())

    val flow = flowOf(1, 2, 3)
    assertSame(flow.castNullable(), flow)
  }

  @Test
  fun testSafeCastSuccess() = runTest {
    assertIs<Flow<Int?>>(
      flowOf<Any?>(1, 2, 3, "hello").safeCast<Int?>(),
    )

    assertIs<Flow<Int?>>(
      flowOf<Any?>(1, 2, 3, "kotlin", null)
        .safeCast<Int?>(),
    )
      .test(
        listOf(
          Event.Value(1),
          Event.Value(2),
          Event.Value(3),
          Event.Value(null),
          Event.Value(null),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testSafeCast() = runTest {
    val stringFlow: Flow<String?> = flowOf("Hello", 42, "World", 123, "Kotlin").safeCast()
    assertIs<Flow<String?>>(stringFlow)

    assertContentEquals(
      listOf("Hello", null, "World", null, "Kotlin"),
      stringFlow.toList(),
    )

    val intFlow: Flow<Int?> = flowOf(1, 2, 3, null).safeCast()
    assertIs<Flow<Int?>>(intFlow)

    assertContentEquals(
      listOf(1, 2, 3, null),
      intFlow.toList(),
    )
  }
}
