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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll

/**
 * Catches exceptions in the flow completion and emits a single [item], then completes normally.
 */
@FlowExtPreview
public fun <T> Flow<T>.catchAndReturn(item: T): Flow<T> =
  catch { emit(item) }

/**
 * Catches exceptions in the flow completion and emits a single [item] provided by [itemSupplier],
 * then completes normally.
 */
@FlowExtPreview
public fun <T> Flow<T>.catchAndReturn(
  itemSupplier: suspend (cause: Throwable) -> T,
): Flow<T> =
  catch { cause -> emit(itemSupplier(cause)) }

/**
 * Catches exceptions in the flow completion and emits all the items from the [fallback] flow.
 * If the [fallback] flow also throws an exception, the exception is not caught and is rethrown.
 */
@FlowExtPreview
public fun <T> Flow<T>.catchAndResume(fallback: Flow<T>): Flow<T> =
  catch { emitAll(fallback) }

/**
 * Catches exceptions in the flow completion and emits all the items provided by [fallbackSupplier].
 * If the fallback flow also throws an exception, the exception is not caught and is rethrown.
 */
@FlowExtPreview
public fun <T> Flow<T>.catchAndResume(
  fallbackSupplier: suspend (cause: Throwable) -> Flow<T>,
): Flow<T> =
  catch { cause -> emitAll(fallbackSupplier(cause)) }
