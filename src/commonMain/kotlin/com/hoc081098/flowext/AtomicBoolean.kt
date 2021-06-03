package com.hoc081098.flowext

public expect class AtomicBoolean(value: Boolean) {
    public fun compareAndSet(expect: Boolean, update: Boolean): Boolean
    public fun set(value: Boolean)
}