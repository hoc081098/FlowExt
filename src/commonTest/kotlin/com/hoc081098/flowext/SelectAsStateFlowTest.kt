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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@ExperimentalCoroutinesApi
private fun <T> Flow<T>.flowOnStandardTestDispatcher(testScope: TestScope): Flow<T> =
  flowOn(StandardTestDispatcher(testScope.testScheduler))

private data class UiState(
  val isLoading: Boolean,
  val items: List<String>,
  val searchTerm: String?,
  val title: String,
  val error: Throwable?,
  val isRefreshing: Boolean,
  val subtitle: String,
  val unreadCount: Int,
) {
  companion object {
    val INITIAL = UiState(
      isLoading = true,
      items = emptyList(),
      searchTerm = null,
      title = "Loading...",
      error = null,
      isRefreshing = false,
      subtitle = "Loading...",
      unreadCount = 0,
    )
  }
}

private fun <T, R> Flow<T>.scanSkipFirst(
  initial: R,
  operation: suspend (acc: R, value: T) -> R,
): Flow<R> = scan(initial, operation).drop(1)

private val zeroToTen = List(10) { it.toString() }

private val reducer: (acc: UiState, value: Int) -> UiState = { state, action ->
  when (action) {
    // items
    0 -> state.copy(items = zeroToTen)
    // loading
    1 -> state.copy(isLoading = !state.isLoading)
    // loading
    2 -> state.copy(isLoading = !state.isLoading)
    // searchTerm
    3 -> state.copy(searchTerm = "4")
    // loading
    4 -> state.copy(isLoading = !state.isLoading)
    // items
    5 -> state.copy(items = state.items + "11")
    // title
    6 -> state.copy(title = "Title")
    // loading
    7 -> state.copy(isLoading = !state.isLoading)
    // error
    8 -> state.copy(error = Throwable("Error"))
    // subtitle
    9 -> state.copy(subtitle = "Subtitle")
    // loading
    10 -> state.copy(isLoading = !state.isLoading)
    // unreadCount
    11 -> state.copy(unreadCount = state.unreadCount + 1)
    // isRefreshing
    12 -> state.copy(isRefreshing = !state.isRefreshing)
    // subtitle
    13 -> state.copy(subtitle = "Subtitle 2")
    // isRefreshing
    14 -> state.copy(isRefreshing = !state.isRefreshing)
    // unreadCount
    15 -> state.copy(unreadCount = state.unreadCount + 1)
    else -> error("Unknown action")
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlow1Test : BaseTest() {
  @Test
  fun testSelectAsStateFlow1() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Eagerly,
      initialValue = 0,
      selector = { it.items.size },
    )

    // Check initial value
    assertEquals(0, selectedStateFlow.value)

    // Update state
    stateFlow.value = stateFlow.value.copy(items = zeroToTen)
    advanceUntilIdle()

    // Check updated value
    assertEquals(10, selectedStateFlow.value)

    // Update with same value - should not emit
    stateFlow.value = stateFlow.value.copy(items = zeroToTen)
    advanceUntilIdle()

    // Should still be 10
    assertEquals(10, selectedStateFlow.value)
  }

  @Test
  fun testSelectAsStateFlow1WithFlow() = runTest {
    val selectedStateFlow = (0..5).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(UiState.INITIAL, reducer)
      .selectAsStateFlow(
        scope = this,
        started = SharingStarted.Eagerly,
        initialValue = emptyList<String>(),
        selector = { it.items },
      )

    // Check initial value
    assertEquals(emptyList(), selectedStateFlow.value)

    advanceUntilIdle()

    // Check final value after all emissions
    assertEquals(zeroToTen + "11", selectedStateFlow.value)
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlow2Test : BaseTest() {
  @Test
  fun testSelectAsStateFlow2() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Eagerly,
      initialValue = emptyList<String>(),
      selector1 = { it.searchTerm },
      selector2 = { it.items },
      projector = { searchTerm, items ->
        items.filter { it.contains(searchTerm ?: "") }
      },
    )

    // Check initial value
    assertEquals(emptyList(), selectedStateFlow.value)

    // Update state with items
    stateFlow.value = stateFlow.value.copy(items = zeroToTen)
    advanceUntilIdle()

    // Should contain all items (empty search term matches all)
    assertEquals(zeroToTen, selectedStateFlow.value)

    // Update search term
    stateFlow.value = stateFlow.value.copy(searchTerm = "4")
    advanceUntilIdle()

    // Should only contain item "4"
    assertEquals(listOf("4"), selectedStateFlow.value)
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlow3Test : BaseTest() {
  @Test
  fun testSelectAsStateFlow3() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Eagerly,
      initialValue = emptyList<String>(),
      selector1 = { it.searchTerm },
      selector2 = { it.items },
      selector3 = { it.title },
      projector = { searchTerm, items, title ->
        items
          .filter { it.contains(searchTerm ?: "") }
          .map { "$it # $title" }
      },
    )

    // Check initial value
    assertEquals(emptyList(), selectedStateFlow.value)

    // Update state
    stateFlow.value = stateFlow.value.copy(
      items = zeroToTen,
      searchTerm = "4",
      title = "MyTitle",
    )
    advanceUntilIdle()

    assertEquals(listOf("4 # MyTitle"), selectedStateFlow.value)
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlow4Test : BaseTest() {
  @Test
  fun testSelectAsStateFlow4() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Eagerly,
      initialValue = emptyList<String>(),
      selector1 = { it.searchTerm },
      selector2 = { it.items },
      selector3 = { it.title },
      selector4 = { it.subtitle },
      projector = { searchTerm, items, title, subtitle ->
        items
          .filter { it.contains(searchTerm ?: "") }
          .map { "$it # $title ~ $subtitle" }
      },
    )

    // Check initial value
    assertEquals(emptyList(), selectedStateFlow.value)

    // Update state
    stateFlow.value = stateFlow.value.copy(
      items = zeroToTen,
      searchTerm = "4",
      title = "MyTitle",
      subtitle = "MySubtitle",
    )
    advanceUntilIdle()

    assertEquals(listOf("4 # MyTitle ~ MySubtitle"), selectedStateFlow.value)
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlow5Test : BaseTest() {
  @Test
  fun testSelectAsStateFlow5() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Eagerly,
      initialValue = emptyList<String>(),
      selector1 = { it.searchTerm },
      selector2 = { it.items },
      selector3 = { it.title },
      selector4 = { it.subtitle },
      selector5 = { it.unreadCount },
      projector = { searchTerm, items, title, subtitle, unreadCount ->
        items
          .filter { it.contains(searchTerm ?: "") }
          .map { "$it # $title ~ $subtitle $ $unreadCount" }
      },
    )

    // Check initial value
    assertEquals(emptyList(), selectedStateFlow.value)

    // Update state
    stateFlow.value = stateFlow.value.copy(
      items = zeroToTen,
      searchTerm = "4",
      title = "MyTitle",
      subtitle = "MySubtitle",
      unreadCount = 5,
    )
    advanceUntilIdle()

    assertEquals(listOf("4 # MyTitle ~ MySubtitle $ 5"), selectedStateFlow.value)
  }
}

@ExperimentalCoroutinesApi
class SelectAsStateFlowSharingTest : BaseTest() {
  @Test
  fun testSelectAsStateFlowSharingLazily() = runTest {
    val stateFlow = MutableStateFlow(UiState.INITIAL)
    var selectorCallCount = 0

    val selectedStateFlow = stateFlow.selectAsStateFlow(
      scope = this,
      started = SharingStarted.Lazily,
      initialValue = 0,
      selector = {
        selectorCallCount++
        it.items.size
      },
    )

    // No collection yet, selector shouldn't be called
    assertEquals(0, selectorCallCount)
    assertEquals(0, selectedStateFlow.value) // Initial value

    // Start collecting
    val collector = selectedStateFlow.replayCache

    // Update state
    stateFlow.value = stateFlow.value.copy(items = zeroToTen)
    advanceUntilIdle()

    // Now selector should be called and value updated
    assertEquals(10, selectedStateFlow.value)
  }
}
