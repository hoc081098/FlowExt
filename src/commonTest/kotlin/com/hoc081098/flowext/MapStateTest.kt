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
import com.hoc081098.flowext.utils.asserReadonlyStateFlow
import com.hoc081098.flowext.utils.test
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowExtPreview
class MapStateTest : BaseTest() {
  @Test
  fun testMapState() = runTest {
    val source = MutableStateFlow(1)
    val mapped: StateFlow<Int> = source.mapState { abs(it) + 1 }

    asserReadonlyStateFlow(mapped, 42)

    launch {
      mapped
        .take(3)
        .test(
          expected = listOf(
            Event.Value(2),
            Event.Value(11),
            Event.Value(1),
            Event.Complete,
          ),
        )
    }

    assertEquals(2, mapped.value)

    source.value = -1
    assertEquals(2, mapped.value)

    source.value = 10
    assertEquals(11, mapped.value)

    source.value = -10
    assertEquals(11, mapped.value)

    source.value = 0
    assertEquals(1, mapped.value)
  }
}
