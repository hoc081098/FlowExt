package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

//
// concat
//

public fun <T> concat(flow1: Flow<T>, flow2: Flow<T>): Flow<T> = flow {
  emitAll(flow1)
  emitAll(flow2)
}

public fun <T> concat(flow1: Flow<T>, flow2: Flow<T>, flow3: Flow<T>): Flow<T> = flow {
  emitAll(flow1)
  emitAll(flow2)
  emitAll(flow3)
}

public fun <T> concat(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>
): Flow<T> =
  flow {
    emitAll(flow1)
    emitAll(flow2)
    emitAll(flow3)
    emitAll(flow4)
  }

public fun <T> concat(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>,
  flow5: Flow<T>
): Flow<T> =
  flow {
    emitAll(flow1)
    emitAll(flow2)
    emitAll(flow3)
    emitAll(flow4)
    emitAll(flow5)
  }

public fun <T> concat(vararg flows: Flow<T>): Flow<T> {
  return when {
    flows.isEmpty() -> emptyFlow()
    flows.size == 1 -> flows[0]
    else -> flow { flows.forEach { emitAll(it) } }
  }
}

//
// concatWith
//

public fun <T> Flow<T>.concatWith(flow: Flow<T>): Flow<T> = concat(this, flow)

public fun <T> Flow<T>.concatWith(flow1: Flow<T>, flow2: Flow<T>): Flow<T> =
  concat(this, flow1, flow2)

public fun <T> Flow<T>.concatWith(flow1: Flow<T>, flow2: Flow<T>, flow3: Flow<T>): Flow<T> =
  concat(this, flow1, flow2, flow3)

public fun <T> Flow<T>.concatWith(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>
): Flow<T> =
  concat(this, flow1, flow2, flow3, flow4)

public fun <T> Flow<T>.concatWith(vararg others: Flow<T>): Flow<T> = concat(this, *others)

//
// startWith
//

public fun <T> Flow<T>.startWithItem(item: T): Flow<T> = onStart { emit(item) }

public fun <T> Flow<T>.startWithArray(vararg items: T): Flow<T> =
  onStart { items.forEach { emit(it) } }

public fun <T> Flow<T>.startWithFlow(other: Flow<T>): Flow<T> = concat(other, this)
