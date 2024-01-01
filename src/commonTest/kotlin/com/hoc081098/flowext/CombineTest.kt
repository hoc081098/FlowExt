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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher

@ExperimentalCoroutinesApi
class CombineTest : BaseTest() {
  @Test
  fun testCombine6() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
    ) { f1, f2, f3, f4, f5, f6 ->
      listOf(f1, f2, f3, f4, f5, f6).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a",
        "2-b-false-b-2-b",
        "3-c-true-c-3-c",
      ),
      list,
    )
  }

  @Test
  fun testCombine7() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
    ) { f1, f2, f3, f4, f5, f6, f7 ->
      listOf(f1, f2, f3, f4, f5, f6, f7).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true",
        "2-b-false-b-2-b-false",
        "3-c-true-c-3-c-true",
      ),
      list,
    )
  }

  @Test
  fun testCombine8() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
    ) { f1, f2, f3, f4, f5, f6, f7, f8 ->
      listOf(f1, f2, f3, f4, f5, f6, f7, f8).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true-a",
        "2-b-false-b-2-b-false-b",
        "3-c-true-c-3-c-true-c",
      ),
      list,
    )
  }

  @Test
  fun testCombine9() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
    ) { f1, f2, f3, f4, f5, f6, f7, f8, f9 ->
      listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true-a-1",
        "2-b-false-b-2-b-false-b-2",
        "3-c-true-c-3-c-true-c-3",
      ),
      list,
    )
  }

  @Test
  fun testCombine10() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
    ) { f1, f2, f3, f4, f5, f6, f7, f8, f9, f10 ->
      listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true-a-1-a",
        "2-b-false-b-2-b-false-b-2-b",
        "3-c-true-c-3-c-true-c-3-c",
      ),
      list,
    )
  }

  @Test
  fun testCombine11() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
    ) { f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11 ->
      listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true-a-1-a-true",
        "2-b-false-b-2-b-false-b-2-b-false",
        "3-c-true-c-3-c-true-c-3-c-true",
      ),
      list,
    )
  }

  @Test
  fun testCombine12() = runTest(StandardTestDispatcher()) {
    val list = combine(
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
      flow1(),
      flow2(),
      flow3(),
      flow4(),
    ) { f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12 ->
      listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12).joinToString(separator = "-")
    }.toList()

    assertEquals(
      listOf(
        "1-a-true-a-1-a-true-a-1-a-true-a",
        "2-b-false-b-2-b-false-b-2-b-false-b",
        "3-c-true-c-3-c-true-c-3-c-true-c",
      ),
      list,
    )
  }
}

private fun flow1(): Flow<Int> = flowOf(1, 2, 3)

private fun flow2(): Flow<String> = flowOf("a", "b", "c")

private fun flow3(): Flow<Boolean> = flowOf(true, false, true)

private fun flow4(): Flow<Char> = flowOf('a', 'b', 'c')
