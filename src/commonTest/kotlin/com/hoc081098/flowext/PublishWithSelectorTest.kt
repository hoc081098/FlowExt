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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

@OptIn(FlowExtPreview::class, ExperimentalCoroutinesApi::class)
class PublishWithSelectorTest : BaseTest() {
  @Test
  fun publishDispatchesSelectedFlows() = runTest {
    val result =
      flow<Any?> {
        delay(100)
        emit(1)
        delay(100)
        emit(2)
        delay(100)
        emit(3)
        delay(100)
        emit("4")
      }
        .publish {
          delay(100)

          merge(
            select { flow ->
              delay(1)
              val sharedFlow = flow.shareIn()

              interval(0L, 100L)
                .flatMapMerge { value ->
                  timer(value, 50L)
                    .withLatestFrom(sharedFlow)
                    .map { it to "shared" }
                }
                .takeUntil(sharedFlow.filter { it == 3 })
            },
            select { flow ->
              flow
                .filterIsInstance<Int>()
                .filter { it % 2 == 0 }
                .map { it to "even" }
                .take(1)
            },
            select { flow ->
              flow
                .filterIsInstance<Int>()
                .filter { it % 2 != 0 }
                .map { it to "odd" }
                .take(1)
            },
            select { flow ->
              flow
                .filterIsInstance<String>()
                .map { it to "string" }
                .take(1)
            },
          )
        }
        .toList()

    assertEquals(
      listOf(Pair(1, "odd"), Pair(2, "even"), Pair("4", "string")),
      result,
    )
  }
}
