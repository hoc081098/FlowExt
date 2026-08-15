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
class MapStateTest : BaseTest() {
  @Test
  fun testMapStatePropertiesAreComputedOnEveryRead() {
    val source = MutableStateFlow(2)
    var invocationCount = 0
    val mapped: StateFlow<Int> = source.mapState {
      ++invocationCount
      it * 10
    }

    assertReadonlyStateFlow(stateFlow = mapped, value = 0)
    assertEquals(expected = 0, actual = invocationCount)

    source.value = 3
    assertEquals(expected = 0, actual = invocationCount)

    assertEquals(expected = 30, actual = mapped.value)
    assertEquals(expected = 1, actual = invocationCount)
    assertEquals(expected = 30, actual = mapped.value)
    assertEquals(expected = 2, actual = invocationCount)
    assertEquals(expected = listOf(30), actual = mapped.replayCache)
    assertEquals(expected = 3, actual = invocationCount)
    assertEquals(expected = listOf(30), actual = mapped.replayCache)
    assertEquals(expected = 4, actual = invocationCount)
  }

  @Test
  fun testMapStatePropertiesAndCollectorsComputeIndependently() = runTest(StandardTestDispatcher()) {
    val source = MutableStateFlow(1)
    var invocationCount = 0
    val mapped = source.mapState {
      ++invocationCount
      it * 2
    }

    val firstCollector = async { mapped.take(2).toList() }
    val secondCollector = async { mapped.take(2).toList() }
    runCurrent()
    assertEquals(expected = 2, actual = invocationCount)

    assertEquals(expected = 2, actual = mapped.value)
    assertEquals(expected = 3, actual = invocationCount)

    source.value = 2
    runCurrent()

    assertContentEquals(expected = listOf(2, 4), actual = firstCollector.await())
    assertContentEquals(expected = listOf(2, 4), actual = secondCollector.await())
    assertEquals(expected = 5, actual = invocationCount)
  }

  @Test
  fun testMapStateCollectionUsesStrongEqualityConflation() = runTest(StandardTestDispatcher()) {
    data class Box(val value: Int)

    val source = MutableStateFlow(1)
    val mapped = source.mapState { Box(abs(it)) }
    val values = mutableListOf<Box>()
    val job = launch { mapped.take(3).toList(values) }
    runCurrent()

    source.value = -1
    runCurrent()
    source.value = 2
    runCurrent()
    source.value = -2
    runCurrent()
    source.value = 1
    runCurrent()
    job.join()

    assertContentEquals(expected = listOf(Box(1), Box(2), Box(1)), actual = values)
  }

  @Test
  fun testMapStateSlowCollectorSkipsIntermediateValues() = runTest(StandardTestDispatcher()) {
    val source = MutableStateFlow(0)
    val mapped = source.mapState { it }
    val values = mutableListOf<Int>()
    val firstEmissionStarted = CompletableDeferred<Unit>()
    val releaseCollector = CompletableDeferred<Unit>()
    val latestReceived = CompletableDeferred<Unit>()

    val job = launch {
      mapped.collect { value ->
        values += value
        if (values.size == 1) {
          firstEmissionStarted.complete(Unit)
          releaseCollector.await()
        }
        if (value == 100) {
          latestReceived.complete(Unit)
        }
      }
    }
    runCurrent()
    firstEmissionStarted.await()

    for (value in 1..100) {
      source.value = value
    }
    runCurrent()
    assertContentEquals(expected = listOf(0), actual = values)
    assertEquals(expected = 100, actual = mapped.value)

    releaseCollector.complete(Unit)
    runCurrent()
    latestReceived.await()
    job.cancelAndJoin()

    assertContentEquals(expected = listOf(0, 100), actual = values)
  }

  @Test
  fun testMapStateSlowCollectorDoesNotReemitEqualLatestValue() = runTest(StandardTestDispatcher()) {
    val source = MutableStateFlow(0)
    val mapped = source.mapState { it }
    val values = mutableListOf<Int>()
    val releaseCollector = CompletableDeferred<Unit>()

    val job = launch {
      mapped.collect { value ->
        values += value
        if (values.size == 1) {
          releaseCollector.await()
        }
      }
    }
    runCurrent()

    source.value = 1
    runCurrent()
    source.value = 0
    runCurrent()

    releaseCollector.complete(Unit)
    runCurrent()
    job.cancelAndJoin()

    assertContentEquals(expected = listOf(0), actual = values)
  }

  @Test
  fun testMapStateTransformFailureEscapesReadAndFailsAffectedCollector() = runTest(StandardTestDispatcher()) {
    val source = MutableStateFlow(0)
    val failure = TestException("Broken!")
    val mapped = source.mapState {
      if (it == 1) throw failure
      it
    }
    var collectionFailure: Throwable? = null

    val job = launch {
      try {
        mapped.collect {}
      } catch (throwable: Throwable) {
        collectionFailure = throwable
      }
    }
    runCurrent()

    source.value = 1
    runCurrent()
    job.join()

    assertEquals(
      expected = failure.message,
      actual = assertIs<TestException>(value = collectionFailure).message,
    )
    assertEquals(
      expected = failure.message,
      actual = assertFailsWith<TestException>(block = { mapped.value }).message,
    )
    assertEquals(
      expected = failure.message,
      actual = assertFailsWith<TestException>(block = { mapped.replayCache }).message,
    )

    source.value = 2
    assertEquals(expected = 2, actual = mapped.value)
    assertEquals(expected = 2, actual = mapped.first())
  }

  @Test
  fun testMapStateCollectionIsCancellable() = runTest(StandardTestDispatcher()) {
    val source = MutableStateFlow(0)
    var invocationCount = 0
    val mapped = source.mapState {
      ++invocationCount
      it
    }
    val collectionStarted = CompletableDeferred<Unit>()
    val collectionCancelled = CompletableDeferred<Unit>()

    val job = launch {
      try {
        mapped.collect { collectionStarted.complete(Unit) }
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

    source.value = 1
    runCurrent()
    assertEquals(expected = 1, actual = invocationCount)
  }
}
