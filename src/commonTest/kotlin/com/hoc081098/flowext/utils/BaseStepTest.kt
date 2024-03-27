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

package com.hoc081098.flowext.utils

import kotlin.test.AfterTest

abstract class BaseStepTest : BaseTest() {
  private var actionIndex = 0
  private var finished = false

  /**
   * Asserts that this invocation is `index`-th in the execution sequence (counting from one).
   */
  protected fun expect(index: Int) {
    val wasIndex = ++actionIndex
    check(index == wasIndex) { "Expecting action index $index but it is actually $wasIndex" }
  }

  /**
   * Asserts that this line is never executed.
   */
  protected fun expectUnreached() {
    error("Should not be reached, current action index is $actionIndex")
  }

  /**
   * Asserts that this is the last action in the test. It must be invoked by any test that used [expect].
   */
  protected fun finish(index: Int) {
    expect(index)
    check(!finished) { "Should call 'finish(...)' at most once" }
    finished = true
  }

  @AfterTest
  fun onCompletion() {
    if (actionIndex != 0 && !finished) {
      error("Expecting that 'finish(${actionIndex + 1})' was invoked, but it was not")
    }
  }
}
