package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun suspendTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(block = block)
