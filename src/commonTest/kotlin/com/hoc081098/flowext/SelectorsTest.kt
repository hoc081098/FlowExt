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
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@ExperimentalCoroutinesApi
private fun <T> Flow<T>.flowOnStandardTestDispatcher(testScope: TestScope): Flow<T> =
  flowOn(StandardTestDispatcher(testScope.testScheduler))

private data class State(
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
    val INITIAL = State(
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

private val reducer: (acc: State, value: Int) -> State = { state, action ->
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
class Select1Test : BaseTest() {
  @Test
  fun testSelect1() = runTest {
    val flow = (0..1_000 step 10)
      .asFlow()
      .select { it.toString().length }

    flow.test((1..4).map { Event.Value(it) } + Event.Complete)
  }

  @Test
  fun testSelect1Distinct() = runTest {
    var count = 0

    val flow = List(10) { i -> List(10) { i } }
      .flatten()
      .asFlow()
      .select {
        count++
        it.toString().length
      }
    flow.test(listOf(Event.Value(1)) + Event.Complete)
    assertEquals(10, count)
  }
}

@ExperimentalCoroutinesApi
class Select2Test : BaseTest() {
  @Test
  fun testSelect2() = runTest {
    var searchTermCount = 0
    var itemsCount = 0
    var projectorCount = 0

    val flow = (0..5).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(State.INITIAL, reducer)
      .select(
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
        },
      )

    flow.test(
      listOf(
        Event.Value(zeroToTen), // 0 - items
        Event.Value(listOf("4")), // 3 - searchTerm
        Event.Complete,
      ),
    )
    assertEquals(6, searchTermCount) // 0..5
    assertEquals(6, itemsCount) // 0..5
    assertEquals(3, projectorCount) // [0 3 5]
  }
}

@ExperimentalCoroutinesApi
class Select3Test : BaseTest() {
  @Test
  fun testSelect3() = runTest {
    var searchTermCount = 0
    var itemsCount = 0
    var titleCount = 0
    var projectorCount = 0

    val flow = (0..7).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(State.INITIAL, reducer)
      .select(
        selector1 = {
          ++searchTermCount
          it.searchTerm
        },
        selector2 = {
          ++itemsCount
          it.items
        },
        selector3 = {
          ++titleCount
          it.title
        },
        projector = { searchTerm, items, title ->
          ++projectorCount
          items
            .filter { it.contains(searchTerm ?: "") }
            .map { "$it # $title" }
        },
      )

    flow.test(
      listOf(
        Event.Value(zeroToTen.map { "$it # Loading..." }), // 0 - items
        Event.Value(listOf("4 # Loading...")), // 3 - searchTerm
        Event.Value(listOf("4 # Title")), // 6 - title
        Event.Complete,
      ),
    )
    assertEquals(8, searchTermCount) // 0..7
    assertEquals(8, itemsCount) // 0..7
    assertEquals(8, titleCount) // 0..7
    assertEquals(4, projectorCount) // [0 3 5 6]
  }
}

@ExperimentalCoroutinesApi
class Select4Test : BaseTest() {
  @Test
  fun testSelect4() = runTest {
    var searchTermCount = 0
    var itemsCount = 0
    var titleCount = 0
    var subtitleCount = 0
    var projectorCount = 0

    val flow = (0..10).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(State.INITIAL, reducer)
      .select(
        selector1 = {
          ++searchTermCount
          it.searchTerm
        },
        selector2 = {
          ++itemsCount
          it.items
        },
        selector3 = {
          ++titleCount
          it.title
        },
        selector4 = {
          ++subtitleCount
          it.subtitle
        },
        projector = { searchTerm, items, title, subtitle ->
          ++projectorCount
          items
            .filter { it.contains(searchTerm ?: "") }
            .map { "$it # $title ~ $subtitle" }
        },
      )

    flow.test(
      listOf(
        Event.Value(zeroToTen.map { "$it # Loading... ~ Loading..." }), // 0 - items
        Event.Value(listOf("4 # Loading... ~ Loading...")), // 3 - searchTerm
        Event.Value(listOf("4 # Title ~ Loading...")), // 6 - title
        Event.Value(listOf("4 # Title ~ Subtitle")), // 9 - subtitle
        Event.Complete,
      ),
    )
    assertEquals(11, searchTermCount) // 0..10
    assertEquals(11, itemsCount) // 0..10
    assertEquals(11, titleCount) // 0..10
    assertEquals(11, subtitleCount) // 0..10
    assertEquals(5, projectorCount) // [0 3 5 6 9]
  }
}

@ExperimentalCoroutinesApi
class Select5Test : BaseTest() {
  @Test
  fun testSelect5() = runTest {
    var searchTermCount = 0
    var itemsCount = 0
    var titleCount = 0
    var subtitleCount = 0
    var unreadCountCount = 0
    var projectorCount = 0

    val flow = (0..15).asFlow()
      .flowOnStandardTestDispatcher(this)
      .onEach { delay(100) }
      .scanSkipFirst(State.INITIAL, reducer)
      .select(
        selector1 = {
          ++searchTermCount
          it.searchTerm
        },
        selector2 = {
          ++itemsCount
          it.items
        },
        selector3 = {
          ++titleCount
          it.title
        },
        selector4 = {
          ++subtitleCount
          it.subtitle
        },
        selector5 = {
          ++unreadCountCount
          it.unreadCount
        },
        projector = { searchTerm, items, title, subtitle, unreadCount ->
          ++projectorCount
          items
            .filter { it.contains(searchTerm ?: "") }
            .map { "$it # $title ~ $subtitle $ $unreadCount" }
        },
      )

    flow.test(
      listOf(
        Event.Value(zeroToTen.map { "$it # Loading... ~ Loading... $ 0" }), // 0 - items
        Event.Value(listOf("4 # Loading... ~ Loading... $ 0")), // 3 - searchTerm
        Event.Value(listOf("4 # Title ~ Loading... $ 0")), // 6 - title
        Event.Value(listOf("4 # Title ~ Subtitle $ 0")), // 9 - subtitle
        Event.Value(listOf("4 # Title ~ Subtitle $ 1")), // 11 - unreadCount
        Event.Value(listOf("4 # Title ~ Subtitle 2 $ 1")), // 13 - subtitle
        Event.Value(listOf("4 # Title ~ Subtitle 2 $ 2")), // 15 - unreadCount
        Event.Complete,
      ),
    )
    assertEquals(16, searchTermCount) // 0..15
    assertEquals(16, itemsCount) // 0..15
    assertEquals(16, titleCount) // 0..15
    assertEquals(16, subtitleCount) // 0..15
    assertEquals(16, unreadCountCount) // 0..15
    assertEquals(8, projectorCount) // [0 3 5 6 9 11 13 15]
  }
}
