package com.hoc081098.flowext

import kotlin.native.concurrent.AtomicInt

internal actual class AtomicBoolean actual constructor(value: Boolean) {
  private val atomic = AtomicInt(value.asInt)

  actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean =
    atomic.compareAndSet(expect.asInt, update.asInt)

  actual var value: Boolean
    get() = atomic.value != 0
    set(value) {
      atomic.value = value.asInt
    }
}

private inline val Boolean.asInt: Int get() = if (this) 1 else 0
