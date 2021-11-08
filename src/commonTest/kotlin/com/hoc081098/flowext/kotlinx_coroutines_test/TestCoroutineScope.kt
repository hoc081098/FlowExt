/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package com.hoc081098.flowext.kotlinx_coroutines_test

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A scope which provides detailed control over the execution of coroutines for tests.
 */
@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
interface TestCoroutineScope : CoroutineScope, UncaughtExceptionCaptor, DelayController {
  /**
   * Call after the test completes.
   * Calls [UncaughtExceptionCaptor.cleanupTestCoroutines] and [DelayController.cleanupTestCoroutines].
   *
   */
  override fun cleanupTestCoroutines()
}

@ExperimentalCoroutinesApi
private class TestCoroutineScopeImpl(
  override val coroutineContext: CoroutineContext
) :
  TestCoroutineScope,
  UncaughtExceptionCaptor by coroutineContext.uncaughtExceptionCaptor,
  DelayController by coroutineContext.delayController {
  override fun cleanupTestCoroutines() {
    coroutineContext.uncaughtExceptionCaptor.cleanupTestCoroutines()
    coroutineContext.delayController.cleanupTestCoroutines()
  }
}

/**
 * A scope which provides detailed control over the execution of coroutines for tests.
 *
 * If the provided context does not provide a [ContinuationInterceptor] (Dispatcher) or [CoroutineExceptionHandler], the
 * scope adds [TestCoroutineDispatcher] and [TestCoroutineExceptionHandler] automatically.
 *
 * @param context an optional context that MAY provide [UncaughtExceptionCaptor] and/or [DelayController]
 */
@InternalCoroutinesApi
@Suppress("FunctionName")
@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
fun TestCoroutineScope(context: CoroutineContext = EmptyCoroutineContext): TestCoroutineScope {
  var safeContext = context
  if (context[ContinuationInterceptor] == null) safeContext += TestCoroutineDispatcher()
  if (context[CoroutineExceptionHandler] == null) safeContext += TestCoroutineExceptionHandler()
  return TestCoroutineScopeImpl(safeContext)
}

@ExperimentalCoroutinesApi
private inline val CoroutineContext.uncaughtExceptionCaptor: UncaughtExceptionCaptor
  get() {
    val handler = this[CoroutineExceptionHandler]
    return handler as? UncaughtExceptionCaptor ?: throw IllegalArgumentException(
      "TestCoroutineScope requires a UncaughtExceptionCaptor such as " +
        "TestCoroutineExceptionHandler as the CoroutineExceptionHandler"
    )
  }

@ExperimentalCoroutinesApi
private inline val CoroutineContext.delayController: DelayController
  get() {
    val handler = this[ContinuationInterceptor]
    return handler as? DelayController ?: throw IllegalArgumentException(
      "TestCoroutineScope requires a DelayController such as TestCoroutineDispatcher as " +
        "the ContinuationInterceptor (Dispatcher)"
    )
  }
