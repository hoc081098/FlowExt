/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
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

package com.hoc081098

import com.hoc081098.flowext.Event
import com.hoc081098.flowext.scanWith
import com.hoc081098.flowext.utils.BaseStepTest
import com.hoc081098.flowext.utils.test
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class ScanWithTest : BaseStepTest() {
  @Test
  fun callInitialSupplierPerCollection() = runTest {
    var initial = 100

    val flow = flowOf(1, 2, 3, 4)
      .scanWith(
        initialSupplier = {
          delay(100)
          ++initial
        },
      ) { acc, value -> acc + value }

    flow.test(
      listOf(
        Event.Value(101),
        Event.Value(101 + 1),
        Event.Value(101 + 1 + 2),
        Event.Value(101 + 1 + 2 + 3),
        Event.Value(101 + 1 + 2 + 3 + 4),
        Event.Complete,
      ),
    )

    flow.test(
      listOf(
        Event.Value(102),
        Event.Value(102 + 1),
        Event.Value(102 + 1 + 2),
        Event.Value(102 + 1 + 2 + 3),
        Event.Value(102 + 1 + 2 + 3 + 4),
        Event.Complete,
      ),
    )
  }
}
