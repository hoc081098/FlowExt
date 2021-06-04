package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope

expect fun suspendTest(block: suspend CoroutineScope.() -> Unit)

fun unreached() {
    throw RuntimeException("Should not reach here!")
}