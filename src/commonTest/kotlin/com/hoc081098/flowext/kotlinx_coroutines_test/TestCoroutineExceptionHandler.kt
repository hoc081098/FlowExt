/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package com.hoc081098.flowext.kotlinx_coroutines_test

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.atomicfu.locks.synchronized as lockSynchronized

/**
 * Access uncaught coroutine exceptions captured during test execution.
 */
@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
interface UncaughtExceptionCaptor {
  /**
   * List of uncaught coroutine exceptions.
   *
   * The returned list is a copy of the currently caught exceptions.
   * During [cleanupTestCoroutines] the first element of this list is rethrown if it is not empty.
   */
  val uncaughtExceptions: List<Throwable>

  /**
   * Call after the test completes to ensure that there were no uncaught exceptions.
   *
   * The first exception in uncaughtExceptions is rethrown. All other exceptions are
   * printed using [Throwable.printStackTrace].
   *
   */
  fun cleanupTestCoroutines()
}

/**
 * An exception handler that captures uncaught exceptions in tests.
 */
@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
class TestCoroutineExceptionHandler :
  AbstractCoroutineContextElement(CoroutineExceptionHandler),
  UncaughtExceptionCaptor,
  CoroutineExceptionHandler {
  private val lock = SynchronizedObject()
  private val _exceptions = mutableListOf<Throwable>()

  /** @suppress **/
  override fun handleException(context: CoroutineContext, exception: Throwable) {
    lockSynchronized(lock) {
      _exceptions += exception
    }
  }

  /** @suppress **/
  override val uncaughtExceptions: List<Throwable>
    get() = lockSynchronized(lock) { _exceptions.toList() }

  /** @suppress **/
  override fun cleanupTestCoroutines() {
    lockSynchronized(lock) {
      val exception = _exceptions.firstOrNull() ?: return
      // log the rest
      _exceptions.drop(1).forEach { it.printStackTrace() }
      throw exception
    }
  }
}
