package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class BufferCountTest : BaseTest() {
  @Test
  fun testBufferCount_shouldEmitBuffersAtBufferSize() = runTest {
    range(0, 10)
      .bufferCount(3)
      .test(
        listOf(
          Event.Value(listOf(0, 1, 2)),
          Event.Value(listOf(3, 4, 5)),
          Event.Value(listOf(6, 7, 8)),
          Event.Value(listOf(9)),
          Event.Complete,
        )
      )
  }

  @Test
  fun testBufferCount_shouldEmitBuffersAtBufferSizeWithStartBufferEvery() = runTest {
    range(0, 8)
      .bufferCount(3, 1)
      .test(
        listOf(
          Event.Value(listOf(0, 1, 2)),
          Event.Value(listOf(1, 2, 3)),
          Event.Value(listOf(2, 3, 4)),
          Event.Value(listOf(3, 4, 5)),
          Event.Value(listOf(4, 5, 6)),
          Event.Value(listOf(5, 6, 7)),
          Event.Value(listOf(6, 7)),
          Event.Value(listOf(7)),
          Event.Complete,
        )
      )

    range(0, 10)
      .bufferCount(2, 4)
      .test(
        listOf(
          Event.Value(listOf(0, 1)),
          Event.Value(listOf(4, 5)),
          Event.Value(listOf(8, 9)),
          Event.Complete,
        )
      )
  }

  @Test
  fun testBufferCount_shouldThrowExceptionWithFailureUpStream() = runTest {
    flow<Int> { throw RuntimeException("Broken!") }
      .bufferCount(2)
      .test(null) {
        assertIs<RuntimeException>(it.single().errorOrThrow())
      }

    flow<Int> { throw RuntimeException("Broken!") }
      .bufferCount(2, 1)
      .test(null) {
        assertIs<RuntimeException>(it.single().errorOrThrow())
      }
  }

  @Test
  fun testBufferCount_testCancellation() = runTest {
    range(0, 10)
      .bufferCount(4)
      .take(2)
      .test(
        listOf(
          Event.Value(listOf(0, 1, 2, 3)),
          Event.Value(listOf(4, 5, 6, 7)),
          Event.Complete,
        )
      )
  }

  @Test
  fun testBufferCount_testCancellationWithStartBufferEvery() = runTest {
    range(0, 10)
      .bufferCount(4, 2)
      .take(2)
      .test(
        listOf(
          Event.Value(listOf(0, 1, 2, 3)),
          Event.Value(listOf(2, 3, 4, 5)),
          Event.Complete,
        )
      )
  }

  @Test
  fun testBufferCount_shouldBufferProperly() = runTest {
    val flow = MutableSharedFlow<Int>(extraBufferCapacity = 64)

    val results = mutableListOf<List<Int>>()
    val job1 = flow.bufferCount(3, 1).onEach {
      results += it
      if (it == listOf(1, 2, 3)) {
        flow.tryEmit(4)
      }
    }.launchIn(this)

    val job2 = launch {
      flow.tryEmit(1)
      flow.tryEmit(2)
      flow.tryEmit(3)
    }

    advanceUntilIdle()
    job1.cancel()
    job2.cancel()

    assertContentEquals(
      results,
      listOf(
        listOf(1, 2, 3),
        listOf(2, 3, 4),
      )
    )
  }
}
