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

package com.hoc081098.flowext.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AtomicRefTest {
  @Test
  fun returns_initial_value_WHEN_created() {
    val ref = AtomicRef(VALUE_1)

    assertEquals(VALUE_1, ref.value)
  }

  @Test
  fun returns_updated_value() {
    val ref = AtomicRef(VALUE_1)

    ref.value = VALUE_2

    assertEquals(VALUE_2, ref.value)
  }

  @Test
  fun compareAndSet_success() {
    val ref = AtomicRef(VALUE_1)
    val result = ref.compareAndSet(VALUE_1, VALUE_2)
    assertTrue(result)
    assertSame(VALUE_2, ref.value)
  }

  @Test
  fun compareAndSet_fail() {
    val ref = AtomicRef(VALUE_1)
    val result = ref.compareAndSet(VALUE_2, VALUE_3)
    assertFalse(result)
    assertSame(VALUE_1, ref.value)
  }

  private companion object {
    private const val VALUE_1 = "a"
    private const val VALUE_2 = "b"
    private const val VALUE_3 = "c"
  }
}
