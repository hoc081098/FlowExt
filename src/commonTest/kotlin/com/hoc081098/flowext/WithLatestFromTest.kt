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
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class WithLatestFromTest : BaseTest() {
  @Test
  fun basic() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to 4,
        "b" to 4,
        "c" to 4,
        "d" to 4,
        "e" to 4,
      ),
    )
  }

  @Test
  fun basicWithNull() = runTest {
    val f1 = flowOf(1, 2, 3, 4, null)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to null,
        "b" to null,
        "c" to null,
        "d" to null,
        "e" to null,
      ),
    )
  }

  @Test
  fun basic2() = runTest {
    val f1 = flowOf(1, 2, 3, 4).onEach { delay(300) }
    val f2 = flowOf("a", "b", "c", "d", "e").onEach { delay(100) }
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "c" to 1,
        "d" to 1,
        "e" to 1,
      ),
    )
  }

  @Test
  fun testWithLatestFrom_failureUpStream() = runTest {
    assertFailsWith<TestException> {
      flow<Int> { throw TestException() }
        .withLatestFrom(neverFlow())
        .collect()
    }

    assertFailsWith<TestException> {
      neverFlow()
        .withLatestFrom(flow<Int> { throw TestException() })
        .collect()
    }
  }

  @Test
  fun testWithLatestFrom_cancellation() = runTest {
    assertFailsWith<CancellationException> {
      flow {
        emit(1)
        throw CancellationException("")
      }
        .withLatestFrom(emptyFlow<Nothing>())
        .collect()
    }

    flowOf(1)
      .withLatestFrom(
        flow {
          emit(2)
          throw CancellationException("")
        },
      )
      .test(
        listOf(
          Event.Value(1 to 2),
          Event.Complete,
        ),
      )
  }

  @Test
  fun cancelOtherFlowAfterSourceFlowCompleted() = runTest {
    flowOf(1)
      .withLatestFrom(neverFlow().startWith(2))
      .test(
        listOf(
          Event.Value(1 to 2),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testWithLatestFrom3() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    assertEquals(
      f2.withLatestFrom(f1, f3).toList(),
      listOf(
        Triple("a", 4, true),
        Triple("b", 4, true),
        Triple("c", 4, true),
        Triple("d", 4, true),
        Triple("e", 4, true),
      ),
    )
  }

  @Test
  fun testWithLatestFrom3WithTransform() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    assertEquals(
      f2.withLatestFrom(f1, f3) { a, b, c -> "$a-$b-$c" }.toList(),
      listOf(
        "a-4-true",
        "b-4-true",
        "c-4-true",
        "d-4-true",
        "e-4-true",
      ),
    )
  }

  @Test
  fun testWithLatestFrom3WithTiming() = runTest {
    val f1 = flowOf(1, 2, 3, 4).onEach { delay(300) }
    val f2 = flowOf("a", "b", "c", "d", "e").onEach { delay(100) }
    val f3 = flowOf(true, false, true).onEach { delay(150) }
    assertEquals(
      f2.withLatestFrom(f1, f3).toList(),
      listOf(
        Triple("c", 1, false),
        Triple("d", 1, false), 
        Triple("e", 1, true),
      ),
    )
  }

  @Test
  fun testWithLatestFrom4() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    assertEquals(
      f2.withLatestFrom(f1, f3, f4) { a, b, c, d -> "$a-$b-$c-$d" }.toList(),
      listOf(
        "a-4-true-z",
        "b-4-true-z",
        "c-4-true-z",
        "d-4-true-z",
        "e-4-true-z",
      ),
    )
  }

  @Test
  fun testWithLatestFrom5() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5) { a, b, c, d, e -> "$a-$b-$c-$d-$e" }.toList(),
      listOf(
        "a-4-true-z-30",
        "b-4-true-z-30",
        "c-4-true-z-30",
        "d-4-true-z-30",
        "e-4-true-z-30",
      ),
    )
  }

  @Test
  fun testWithLatestFrom6() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    val f6 = flowOf("first", "second", "third")
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5, f6) { a, b, c, d, e, f -> "$a-$b-$c-$d-$e-$f" }.toList(),
      listOf(
        "a-4-true-z-30-third",
        "b-4-true-z-30-third",
        "c-4-true-z-30-third",
        "d-4-true-z-30-third",
        "e-4-true-z-30-third",
      ),
    )
  }

  @Test
  fun testWithLatestFrom3_failureUpStream() = runTest {
    assertFailsWith<TestException> {
      flow<Int> { throw TestException() }
        .withLatestFrom(neverFlow(), neverFlow())
        .collect()
    }

    assertFailsWith<TestException> {
      neverFlow()
        .withLatestFrom(flow<Int> { throw TestException() }, neverFlow())
        .collect()
    }

    assertFailsWith<TestException> {
      neverFlow()
        .withLatestFrom(neverFlow(), flow<Int> { throw TestException() })
        .collect()
    }
  }

  @Test
  fun testWithLatestFrom3_cancellation() = runTest {
    assertFailsWith<CancellationException> {
      flow {
        emit(1)
        throw CancellationException("")
      }
        .withLatestFrom(emptyFlow<Nothing>(), emptyFlow<Nothing>())
        .collect()
    }

    flowOf(1)
      .withLatestFrom(
        flow {
          emit(2)
          throw CancellationException("")
        },
        flowOf(3),
      )
      .test(
        listOf(
          Event.Value(Triple(1, 2, 3)),
          Event.Complete,
        ),
      )
  }

  @Test
  fun testWithLatestFrom3_nullValues() = runTest {
    val f1 = flowOf(1, 2, 3, 4, null)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, null)
    assertEquals(
      f2.withLatestFrom(f1, f3).toList(),
      listOf(
        Triple("a", null, null),
        Triple("b", null, null),
        Triple("c", null, null),
        Triple("d", null, null),
        Triple("e", null, null),
      ),
    )
  }

  @Test
  fun testWithLatestFrom7() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    val f6 = flowOf("first", "second", "third")
    val f7 = flowOf(100L, 200L, 300L)
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5, f6, f7) { a, b, c, d, e, f, g -> "$a-$b-$c-$d-$e-$f-$g" }.toList(),
      listOf(
        "a-4-true-z-30-third-300",
        "b-4-true-z-30-third-300",
        "c-4-true-z-30-third-300",
        "d-4-true-z-30-third-300",
        "e-4-true-z-30-third-300",
      ),
    )
  }

  @Test
  fun testWithLatestFrom8() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    val f6 = flowOf("first", "second", "third")
    val f7 = flowOf(100L, 200L, 300L)
    val f8 = flowOf(0.1f, 0.2f, 0.3f)
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5, f6, f7, f8) { a, b, c, d, e, f, g, h -> "$a-$b-$c-$d-$e-$f-$g-$h" }.toList(),
      listOf(
        "a-4-true-z-30-third-300-0.3",
        "b-4-true-z-30-third-300-0.3",
        "c-4-true-z-30-third-300-0.3",
        "d-4-true-z-30-third-300-0.3",
        "e-4-true-z-30-third-300-0.3",
      ),
    )
  }

  @Test
  fun testWithLatestFrom9() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    val f6 = flowOf("first", "second", "third")
    val f7 = flowOf(100L, 200L, 300L)
    val f8 = flowOf(0.1f, 0.2f, 0.3f)
    val f9 = flowOf("i", "ii", "iii")
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5, f6, f7, f8, f9) { a, b, c, d, e, f, g, h, i -> "$a-$b-$c-$d-$e-$f-$g-$h-$i" }.toList(),
      listOf(
        "a-4-true-z-30-third-300-0.3-iii",
        "b-4-true-z-30-third-300-0.3-iii",
        "c-4-true-z-30-third-300-0.3-iii",
        "d-4-true-z-30-third-300-0.3-iii",
        "e-4-true-z-30-third-300-0.3-iii",
      ),
    )
  }

  @Test
  fun testWithLatestFrom10() = runTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    val f3 = flowOf(true, false, true)
    val f4 = flowOf('x', 'y', 'z')
    val f5 = flowOf(10.0, 20.0, 30.0)
    val f6 = flowOf("first", "second", "third")
    val f7 = flowOf(100L, 200L, 300L)
    val f8 = flowOf(0.1f, 0.2f, 0.3f)
    val f9 = flowOf("i", "ii", "iii")
    val f10 = flowOf(42, 43, 44)
    assertEquals(
      f2.withLatestFrom(f1, f3, f4, f5, f6, f7, f8, f9, f10) { a, b, c, d, e, f, g, h, i, j -> "$a-$b-$c-$d-$e-$f-$g-$h-$i-$j" }.toList(),
      listOf(
        "a-4-true-z-30-third-300-0.3-iii-44",
        "b-4-true-z-30-third-300-0.3-iii-44",
        "c-4-true-z-30-third-300-0.3-iii-44",
        "d-4-true-z-30-third-300-0.3-iii-44",
        "e-4-true-z-30-third-300-0.3-iii-44",
      ),
    )
  }
}
