package com.hoc081098.flowext.internal

internal expect class AtomicBoolean(value: Boolean = false) {
  fun compareAndSet(expect: Boolean, update: Boolean): Boolean
  var value: Boolean
}
