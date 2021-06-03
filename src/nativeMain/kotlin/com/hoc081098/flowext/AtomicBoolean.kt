package com.hoc081098.flowext

import kotlin.native.concurrent.FreezableAtomicReference as KAtomicRef

public actual class AtomicBoolean actual constructor(value: Boolean) {
    private val atomic = KAtomicRef(value)

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean = atomic.compareAndSet(expect, update)

    public actual fun set(value: Boolean) {
        atomic.value = value
    }
}