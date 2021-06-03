package com.hoc081098.flowext

expect class AtomicBoolean(value: Boolean = false) {
    fun compareAndSet(expect: Boolean, update: Boolean): Boolean
    var value: Boolean
}