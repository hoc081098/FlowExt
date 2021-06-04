package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

@DelicateCoroutinesApi
actual fun suspendTest(block: suspend CoroutineScope.() -> Unit): dynamic {
    return GlobalScope.promise(block = block)
}