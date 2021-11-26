package com.hoc081098.flowext.internal

internal actual class AtomicBoolean actual constructor(actual var value: Boolean) {
  actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean = if (value == expect) {
    value = update
    true
  } else {
    false
  }
}
