package com.hoc081098.flowext.internal

import java.util.concurrent.atomic.AtomicBoolean as JavaAtomicBoolean

internal actual class AtomicBoolean actual constructor(value: Boolean) {
  private val atomic = JavaAtomicBoolean(value)

  actual var value: Boolean
    get() = atomic.get()
    set(value) = atomic.set(value)

  actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean =
    atomic.compareAndSet(expect, update)
}
