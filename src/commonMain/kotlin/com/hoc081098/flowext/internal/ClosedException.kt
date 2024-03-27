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

// Reference: https://github.com/Kotlin/kotlinx.coroutines/blob/8c516f5ab1fcc39629d2838489598135eedd7b80/kotlinx-coroutines-core/common/src/flow/internal/FlowExceptions.common.kt
// Change: We don't inherit from `kotlinx.coroutines.CancellationException`.

/**
 * This exception is thrown when an operator needs no more elements from the flow.
 *
 * The operator should never allow this exception to be thrown past its own boundary.
 * This exception can be safely ignored by non-terminal flow operator
 * if and only if it was caught by its owner (see usages of [checkOwnership]).
 *
 * Therefore, the [owner] parameter must be unique for every invocation of every operator.
 */
internal expect class ClosedException(owner: Any) : Exception {
  val owner: Any
}

internal fun ClosedException.checkOwnership(owner: Any) {
  if (this.owner !== owner) throw this
}
