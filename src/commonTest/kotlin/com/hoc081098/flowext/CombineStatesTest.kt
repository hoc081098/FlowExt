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
import com.hoc081098.flowext.utils.assertReadonlyStateFlow
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent

@ExperimentalCoroutinesApi
@FlowExtPreview
class CombineStatesTest : BaseTest() {
  @Test
  fun testCombineStatesPropertiesAreComputedOnEveryRead() {
    val first = MutableStateFlow(2)
    val second = MutableStateFlow(3)
    var invocationCount = 0
    val combined: StateFlow<Int> = combineStates(first, second) { a, b ->
      ++invocationCount
      a * 100 + b
    }

    assertReadonlyStateFlow(stateFlow = combined, value = 0)
    assertEquals(expected = 0, actual = invocationCount)

    first.value = 4
    second.value = 5
    assertEquals(expected = 0, actual = invocationCount)

    assertEquals(expected = 405, actual = combined.value)
    assertEquals(expected = 1, actual = invocationCount)
    assertEquals(expected = 405, actual = combined.value)
    assertEquals(expected = 2, actual = invocationCount)
    assertEquals(expected = listOf(405), actual = combined.replayCache)
    assertEquals(expected = 3, actual = invocationCount)
    assertEquals(expected = listOf(405), actual = combined.replayCache)
    assertEquals(expected = 4, actual = invocationCount)
  }

  @Test
  fun testCombineStatesCollectionUsesStrongEqualityConflation() = runTest(StandardTestDispatcher()) {
    data class Box(val value: Int)

    val first = MutableStateFlow(1)
    val second = MutableStateFlow(1)
    val combined = combineStates(first, second) { a, b -> Box(abs(a) + abs(b)) }
    val values = mutableListOf<Box>()
    val job = launch { combined.take(3).toList(values) }
    runCurrent() // Let the initial value be collected.

    first.value = -1
    runCurrent()
    second.value = -1
    runCurrent()
    first.value = 2
    runCurrent()
    first.value = -2
    runCurrent()
    second.value = 0
    runCurrent()
    job.join()

    assertContentEquals(expected = listOf(Box(2), Box(3), Box(2)), actual = values)
  }

  @Test
  fun testCombineStatesSlowCollectorSkipsIntermediateValues() = runTest(StandardTestDispatcher()) {
    val first = MutableStateFlow(0)
    val second = MutableStateFlow(0)
    val combined = combineStates(first, second, ::Pair)
    val releaseCollector = CompletableDeferred<Unit>()
    val values = mutableListOf<Pair<Int, Int>>()

    val job = launch {
      combined.collect { value ->
        values += value
        if (values.size == 1) {
          releaseCollector.await() // Suspend so the source updates below are conflated while this collector is busy.
        }
      }
    }
    runCurrent()

    first.value = 1
    second.value = 1
    runCurrent()
    first.value = 2
    second.value = 2
    runCurrent()
    first.value = 10
    second.value = 10
    runCurrent()

    // Release the collector so it resumes and collects the latest value, (10, 10).
    releaseCollector.complete(Unit)
    runCurrent()
    job.cancelAndJoin()

    assertContentEquals(expected = listOf(0 to 0, 10 to 10), actual = values)
  }

  @Test
  fun testCombineStatesConflatesBeforeApplyingEquality() = runTest(StandardTestDispatcher()) {
    val first = MutableStateFlow(0)
    val second = MutableStateFlow(0)
    val combined = combineStates(first, second, Int::plus)
    val releaseCollector = CompletableDeferred<Unit>()
    val values = mutableListOf<Int>()

    val job = launch {
      combined.collect { value ->
        values += value
        if (values.size == 1) {
          releaseCollector.await() // Suspend so the source updates below are conflated while this collector is busy.
        }
      }
    }
    runCurrent()

    first.value = 1 // Conflated away while the collector is suspended.
    runCurrent()
    first.value = 0 // Not reemitted: it equals the last emitted value (0).
    runCurrent()

    // Release the collector so it resumes.
    releaseCollector.complete(Unit)
    runCurrent()
    job.cancelAndJoin()

    // Only the initial value was collected; the intermediate update was conflated away.
    assertContentEquals(expected = listOf(0), actual = values)
  }

  @Test
  fun testCombineStatesPropertiesAndCollectorsComputeIndependently() = runTest(StandardTestDispatcher()) {
    val first = MutableStateFlow(0)
    val second = MutableStateFlow(0)
    var invocationCount = 0
    val combined = combineStates(first, second) { a, b ->
      ++invocationCount
      a + b
    }

    val firstCollector = async { combined.take(2).toList() }
    val secondCollector = async { combined.take(2).toList() }
    runCurrent() // Let both collectors start and invoke transform once each for the initial value.
    assertEquals(expected = 2, actual = invocationCount)

    assertEquals(expected = 0, actual = combined.value)
    assertEquals(expected = 3, actual = invocationCount)

    first.value = 1
    runCurrent() // Let both collectors observe the updated value.

    assertContentEquals(expected = listOf(0, 1), actual = firstCollector.await())
    assertContentEquals(expected = listOf(0, 1), actual = secondCollector.await())
    assertEquals(expected = 5, actual = invocationCount)
  }

  @Test
  fun testCombineStatesTransformFailureEscapesReadAndFailsAffectedCollector() = runTest(StandardTestDispatcher()) {
    val first = MutableStateFlow(0)
    val second = MutableStateFlow(0)
    val failure = TestException("Broken!")
    val combined = combineStates(first, second) { a, b ->
      if (a == 1) throw failure
      a + b
    }
    var collectionFailure: Throwable? = null

    // The collector fails when transform throws.
    val job = launch {
      try {
        combined.collect {}
      } catch (throwable: Throwable) {
        collectionFailure = throwable
      }
    }
    runCurrent()

    first.value = 1
    runCurrent()
    job.join()

    // Reading value/replayCache after the failure also rethrows it.
    assertEquals(
      expected = failure.message,
      actual = assertIs<TestException>(value = collectionFailure).message,
    )
    assertEquals(
      expected = failure.message,
      actual = assertFailsWith<TestException>(block = { combined.value }).message,
    )
    assertEquals(
      expected = failure.message,
      actual = assertFailsWith<TestException>(block = { combined.replayCache }).message,
    )

    // A later, non-throwing value is unaffected by the earlier failure.
    first.value = 2
    assertEquals(expected = 2, actual = combined.value)
    assertEquals(expected = 2, actual = combined.first())
  }

  @Test
  fun testCombineStatesCollectionIsCancellable() = runTest(StandardTestDispatcher()) {
    val first = MutableStateFlow(0)
    val second = MutableStateFlow(0)
    var invocationCount = 0
    val combined = combineStates(first, second) { a, b ->
      ++invocationCount
      a + b
    }
    val collectionStarted = CompletableDeferred<Unit>()
    val collectionCancelled = CompletableDeferred<Unit>()

    val job = launch {
      try {
        combined.collect { collectionStarted.complete(Unit) }
      } finally {
        collectionCancelled.complete(Unit)
      }
    }
    runCurrent()
    collectionStarted.await()
    assertEquals(expected = 1, actual = invocationCount)
    assertTrue(actual = job.isActive)

    job.cancelAndJoin()
    assertTrue(actual = job.isCancelled)
    assertTrue(actual = collectionCancelled.isCompleted)

    // Since the collection was cancelled, the new source value is not collected.
    first.value = 1
    runCurrent()
    assertEquals(expected = 1, actual = invocationCount)
  }

  @Test
  fun testCombineStatesArity3Through12WiresSourcesInOrder() = runTest {
    data class Case(
      val arity: Int,
      val expected: String,
      val state: StateFlow<String>,
    )

    fun concat(vararg values: String): String = values.joinToString(separator = "")

    val sources = (1..12).map { MutableStateFlow("<$it>") }
    val cases = listOf(
      Case(
        arity = 3,
        expected = "<1><2><3>",
        state = combineStates(sources[0], sources[1], sources[2]) { v1, v2, v3 -> concat(v1, v2, v3) },
      ),
      Case(
        arity = 4,
        expected = "<1><2><3><4>",
        state = combineStates(sources[0], sources[1], sources[2], sources[3]) { v1, v2, v3, v4 ->
          concat(v1, v2, v3, v4)
        },
      ),
      Case(
        arity = 5,
        expected = "<1><2><3><4><5>",
        state = combineStates(sources[0], sources[1], sources[2], sources[3], sources[4]) { v1, v2, v3, v4, v5 ->
          concat(v1, v2, v3, v4, v5)
        },
      ),
      Case(
        arity = 6,
        expected = "<1><2><3><4><5><6>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
        ) { v1, v2, v3, v4, v5, v6 -> concat(v1, v2, v3, v4, v5, v6) },
      ),
      Case(
        arity = 7,
        expected = "<1><2><3><4><5><6><7>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
        ) { v1, v2, v3, v4, v5, v6, v7 -> concat(v1, v2, v3, v4, v5, v6, v7) },
      ),
      Case(
        arity = 8,
        expected = "<1><2><3><4><5><6><7><8>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
          sources[7],
        ) { v1, v2, v3, v4, v5, v6, v7, v8 -> concat(v1, v2, v3, v4, v5, v6, v7, v8) },
      ),
      Case(
        arity = 9,
        expected = "<1><2><3><4><5><6><7><8><9>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
          sources[7],
          sources[8],
        ) { v1, v2, v3, v4, v5, v6, v7, v8, v9 -> concat(v1, v2, v3, v4, v5, v6, v7, v8, v9) },
      ),
      Case(
        arity = 10,
        expected = "<1><2><3><4><5><6><7><8><9><10>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
          sources[7],
          sources[8],
          sources[9],
        ) { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10 ->
          concat(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
        },
      ),
      Case(
        arity = 11,
        expected = "<1><2><3><4><5><6><7><8><9><10><11>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
          sources[7],
          sources[8],
          sources[9],
          sources[10],
        ) { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11 ->
          concat(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)
        },
      ),
      Case(
        arity = 12,
        expected = "<1><2><3><4><5><6><7><8><9><10><11><12>",
        state = combineStates(
          sources[0],
          sources[1],
          sources[2],
          sources[3],
          sources[4],
          sources[5],
          sources[6],
          sources[7],
          sources[8],
          sources[9],
          sources[10],
          sources[11],
        ) { v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12 ->
          concat(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)
        },
      ),
    )

    for ((arity, expected, state) in cases) {
      assertEquals(
        expected = expected,
        actual = state.value,
        message = "arity=$arity property value",
      )
      assertEquals(
        expected = expected,
        actual = state.first(),
        message = "arity=$arity collected value",
      )
    }
  }
}
