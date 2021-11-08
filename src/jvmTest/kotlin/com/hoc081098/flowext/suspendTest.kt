package com.hoc081098.flowext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

val testCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

actual fun suspendTest(block: suspend CoroutineScope.() -> Unit) {
  runBlocking(
    context = testCoroutineDispatcher,
    block = block
  )
}
