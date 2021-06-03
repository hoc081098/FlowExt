package com.hoc081098.flowext

public actual class AtomicBoolean actual constructor(value: Boolean) {
    private val atomic = java.util.concurrent.atomic.AtomicBoolean(value)

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean = atomic.compareAndSet(expect, update)

    public actual fun set(value: Boolean): Unit = atomic.set(value)
}