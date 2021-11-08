package com.hoc081098.flowext

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking

actual fun suspendTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(
  context = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
  block = block
)
