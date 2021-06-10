package com.hoc081098.flowext

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise

private object GlobalScope : CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main
}

actual fun suspendTest(block: suspend CoroutineScope.() -> Unit): dynamic =
  GlobalScope.promise(block = block)
