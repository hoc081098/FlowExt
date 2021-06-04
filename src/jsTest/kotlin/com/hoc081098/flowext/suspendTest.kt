package com.hoc081098.flowext

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.promise

private object GlobalScope : CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = EmptyCoroutineContext
}

@DelicateCoroutinesApi
actual fun suspendTest(block: suspend CoroutineScope.() -> Unit): dynamic {
  return GlobalScope.promise(block = block)
}
