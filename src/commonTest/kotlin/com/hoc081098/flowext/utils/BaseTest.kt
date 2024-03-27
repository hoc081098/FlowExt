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

import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * The default timeout to use when running a test.
 */
internal val DEFAULT_TIMEOUT = 10.seconds

abstract class BaseTest {
  @ExperimentalCoroutinesApi
  protected fun runTest(
    testDispatcher: TestDispatcher? = null,
    timeout: Duration = DEFAULT_TIMEOUT,
    testBody: suspend TestScope.() -> Unit,
  ): TestResult {
    return kotlinx.coroutines.test.runTest(
      context = testDispatcher
        ?: UnconfinedTestDispatcher(name = "${this::class.simpleName}-dispatcher"),
      timeout = timeout,
      testBody = testBody,
    )
  }
}

suspend inline fun hang(onCancellation: () -> Unit) {
  try {
    suspendCancellableCoroutine<Unit> { }
  } finally {
    onCancellation()
  }
}

suspend inline fun <reified T : Throwable> assertFailsWith(flow: Flow<*>) {
  try {
    flow.collect()
    fail("Should be unreached")
  } catch (e: Throwable) {
    assertTrue(e is T, "Expected exception ${T::class}, but had $e instead")
  }
}

suspend fun Flow<Int>.sum() = fold(0) { acc, value -> acc + value }
