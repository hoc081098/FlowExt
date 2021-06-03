package com.hoc081098.flowext

public actual class AtomicBoolean actual constructor(private var value: Boolean) {
    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        if (value == expect) {
            value = update
            return true
        }
        return false
    }

    public actual fun set(value: Boolean) {
        this.value = value
    }
}