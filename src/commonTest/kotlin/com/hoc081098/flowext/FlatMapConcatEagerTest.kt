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
import com.hoc081098.flowext.utils.NamedDispatchers
import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.assertFailsWith
import com.hoc081098.flowext.utils.hang
import com.hoc081098.flowext.utils.sum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.yield

@FlowPreview
@ExperimentalCoroutinesApi
class FlatMapConcatEagerTest : BaseStepTest() {
  @Test
  fun testMapEagerMapsConcurrently() = runTest {
    launch {
      (2..5).asFlow()
        .mapEager(4) {
          delay(2.seconds)
          it
        }
        .collect { value ->
          expect(value)
        }
    }
    delay(1.seconds)
    expect(1)
    delay(2.seconds)
    finish(6)
  }

  @Test
  fun testMapEagerConcurrencyOne() = runTest {
    launch {
      (2..6 step 2).asFlow()
        .mapEager(1) {
          delay(2.seconds)
          it
        }
        .collect { value ->
          expect(value)
        }
    }
    delay(1.seconds)
    (1..5 step 2).forEach { value ->
      expect(value)
      delay(2.seconds)
    }
    finish(7)
  }

  @Test
  fun testMapEagerSavesOrder() = runTest {
    val result = (3 downTo 1).asFlow()
      .mapEager { value ->
        delay(value.seconds)
        value
      }
      .toList()
    assertEquals(listOf(3, 2, 1), result)
  }

  @Test
  fun testFlatMap() = runTest {
    val n = 100
    val sum = (1..n).asFlow()
      .flatMapConcatEager { value ->
        // 1 + (1 + 2) + (1 + 2 + 3) + ... (1 + .. + n)
        flow {
          repeat(value) {
            emit(it + 1)
          }
        }
      }.sum()

    assertEquals(n * (n + 1) * (n + 2) / 6, sum)
  }

  @Test
  fun testFlatMapBufferSuspends() = runTest {
    val result = (3 downTo 1).asFlow()
      .flatMapConcatEager(bufferSize = 1) { value ->
        flow {
          emit(value)
          // 3, 2, 1 -> 1, 2, 3
          expect(4 - value)
          delay(value.seconds)
          // 1, 2, 3 -> 4, 5, 6
          expect(value + 3)
          emit(value) // bufferSize == 1 suspends that call for "2" and "1"
          // 3, 2, 1 -> 7, 8, 9
          expect(10 - value)
        }
      }
      .toList()
    finish(10)
    assertEquals(listOf(3, 3, 2, 2, 1, 1), result)
  }

  @Test
  fun testSingle() = runTest {
    val flow = flow {
      repeat(100) {
        emit(it)
      }
    }.flatMapConcatEager { value ->
      if (value == 99) {
        flowOf(42)
      } else {
        flowOf()
      }
    }

    val value = flow.single()
    assertEquals(42, value)
  }

  @Test
  fun testNulls() = runTest {
    val list = flowOf(1, null, 2).flatMapConcatEager {
      flowOf(1, null, null, 2)
    }.toList()

    assertEquals(List(3) { listOf(1, null, null, 2) }.flatten(), list)
  }

  @Test
  fun testContext() = runTest {
    val captured = ArrayList<String>()
    val flow = flowOf(1)
      .flowOn(NamedDispatchers("irrelevant"))
      .flatMapConcatEager {
        captured += NamedDispatchers.name()
        flow {
          captured += NamedDispatchers.name()
          emit(it)
        }
      }

    flow.flowOn(NamedDispatchers("1")).sum()
    flow.flowOn(NamedDispatchers("2")).sum()
    assertEquals(listOf("1", "1", "2", "2"), captured)
  }

  @Test
  fun testIsolatedContext() = runTest {
    val flow = flowOf(1)
      .flowOn(NamedDispatchers("irrelevant"))
      .flatMapConcatEager {
        flow {
          assertEquals("inner", NamedDispatchers.name())
          emit(it)
        }
      }.flowOn(NamedDispatchers("inner"))
      .flatMapConcatEager {
        flow {
          assertEquals("outer", NamedDispatchers.name())
          emit(it)
        }
      }.flowOn(NamedDispatchers("outer"))

    assertEquals(1, flow.singleOrNull())
  }

  @Test
  fun testFlatMapConcurrency() = runTest {
    var concurrentRequests = 0
    val flow = (1..100).asFlow().flatMapConcatEager(concurrency = 2) { value ->
      flow {
        ++concurrentRequests
        emit(value)
        delay(Long.MAX_VALUE)
      }
    }

    val consumer = launch {
      flow.collect { value ->
        expect(value)
      }
    }

    repeat(5) {
      yield()
    }

    assertEquals(2, concurrentRequests)
    consumer.cancelAndJoin()
    finish(2) // first value will be received, but other blocked by first flow
  }

  @Test
  fun testCancellationExceptionDownstream() = runTest {
    val flow = flowOf(1, 2, 3).flatMapConcatEager {
      flow {
        emit(it)
        throw CancellationException("")
      }
    }

    assertEquals(listOf(1, 2, 3), flow.toList())
  }

  @Test
  fun testCancellationExceptionUpstream() = runTest {
    val flow = flow {
      expect(1)
      emit(1)
      expect(2)
      yield()
      throw CancellationException("")
    }.flatMapConcatEager {
      flow {
        expect(3)
        emit(it)
        hang { expect(4) }
      }
    }

    assertFailsWith<CancellationException>(flow)
    finish(5)
  }

  @Test
  fun testCancellation() = runTest(StandardTestDispatcher()) {
    val result = flow {
      emit(1)
      emit(2)
      emit(3)
      emit(4)
      expectUnreached() // Cancelled by take
      emit(5)
    }.flatMapConcatEager(2) { v ->
      flow { emit(v) }
    }
      .take(2)
      .toList()
    assertEquals(listOf(1, 2), result)
  }

  @Test
  fun testFailureCancellation() = runTest {
    val flow = flow {
      expect(2)
      emit(1)
      expect(3)
      emit(2)
      expect(4)
    }.flatMapConcatEager {
      if (it == 1) {
        flow {
          hang { expect(6) }
        }
      } else {
        flow<Int> {
          expect(5)
          throw TestException()
        }
      }
    }

    expect(1)
    assertFailsWith(TestException::class) { flow.singleOrNull() }
    finish(7)
  }

  @Test
  fun testConcurrentFailure() = runTest {
    val latch = Channel<Unit>()
    val flow = flow {
      expect(2)
      emit(1)
      expect(3)
      emit(2)
    }.flatMapConcatEager {
      if (it == 1) {
        flow<Int> {
          expect(5)
          latch.send(Unit)
          hang {
            expect(7)
            throw TestException()
          }
        }
      } else {
        expect(4)
        latch.receive()
        expect(6)
        throw TestException()
      }
    }

    expect(1)
    assertFailsWith<TestException>(flow)
    finish(8)
  }

  @Test
  fun testFailureInMapOperationCancellation() = runTest {
    val latch = Channel<Unit>()
    val flow: Flow<Unit> = flow {
      expect(2)
      emit(1)
      expect(3)
      emit(2)
      expectUnreached()
    }.flatMapConcatEager {
      if (it == 1) {
        flow {
          expect(5)
          latch.send(Unit)
          hang { expect(7) }
        }
      } else {
        expect(4)
        latch.receive()
        expect(6)
        throw TestException()
      }
    }

    expect(1)
    assertFailsWith<TestException> { flow.count() }
    finish(8)
  }
}
