package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

actual fun suspendTest(block: suspend CoroutineScope.() -> Unit): dynamic =
  MainScope().promise(block = block)
