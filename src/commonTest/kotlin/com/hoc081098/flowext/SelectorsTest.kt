/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
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
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@ExperimentalCoroutinesApi
private fun <T> Flow<T>.flowOnStandardTestDispatcher(testScope: TestScope): Flow<T> =
  flowOn(StandardTestDispatcher(testScope.testScheduler))

@ExperimentalCoroutinesApi
private suspend fun <T> TestScope.useScope(f: suspend CoroutineScope.() -> T): T {
  val coroutineScope = CoroutineScope(StandardTestDispatcher(testScheduler))
  try {
    return coroutineScope.f()
  } finally {
    coroutineScope.cancel()
  }
}

private data class State(
  val isLoading: Boolean,
  val items: List<String>,
  val searchTerm: String?
)

private fun <T, R> Flow<T>.scanSkipFirst(
  initial: R,
  operation: suspend (acc: R, value: T) -> R
): Flow<R> = scan(initial, operation).drop(1)

@ExperimentalCoroutinesApi
class SelectorsTest : BaseTest() {
  @Test
  fun testSelect() = runTest {
    val flow = (0..1_000 step 10)
      .asFlow()
      .select { it.toString().length }

    flow.test((1..4).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun testSelect2() = runTest {
    var searchTermCount = 0
    var itemsCount = 0
    var projectorCount = 0

    val flow = (0..5).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(
        State(
          isLoading = true,
          items = emptyList(),
          searchTerm = null
        )
      ) { state, action ->
        when (action) {
          // items
          0 -> state.copy(items = List(10) { it.toString() })
          // loading
          1 -> state.copy(isLoading = !state.isLoading)
          // loading
          2 -> state.copy(isLoading = !state.isLoading)
          // searchTerm
          3 -> state.copy(searchTerm = "4")
          // loading
          4 -> state.copy(isLoading = !state.isLoading)
          // loading
          5 -> state.copy(isLoading = !state.isLoading)
          else -> error("Unknown action")
        }
      }
      .select2(
        selector1 = {
          ++searchTermCount
          it.searchTerm
        },
        selector2 = {
          ++itemsCount
          it.items
        },
        projector = { searchTerm, items ->
          ++projectorCount
          items.filter { it.contains(searchTerm ?: "") }
        }
      )

    flow.test(
      listOf(
        Event.Value(List(10) { it.toString() }),
        Event.Value(listOf("4")),
        Event.Complete
      )
    )
    assertEquals(6, searchTermCount) // 0..5
    assertEquals(6, itemsCount) // 0..5
    assertEquals(2, projectorCount) // [0 3]
  }

  // ----------------------------------------------

  @Test
  fun testSelectAsStateFlow() = runTest {
    useScope {
      val stateFlow = (0..1_000 step 10)
        .asFlow()
        .flowOnStandardTestDispatcher(this@runTest)
        .onEach { delay(100) }
        .stateIn(
          scope = this,
          started = SharingStarted.WhileSubscribed(),
          initialValue = 0
        )
        .selectAsStateFlow(
          scope = this,
          started = SharingStarted.WhileSubscribed()
        ) { it.toString().length }

      assertEquals(1, stateFlow.value)
      stateFlow
        .take(4)
        .test((1..4).map { Event.Value(it) } + Event.Complete)
      assertEquals(4, stateFlow.value)
    }
  }
}
