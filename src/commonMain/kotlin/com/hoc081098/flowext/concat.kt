/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Petrus Nguyễn Thái Học
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

//
// concat
//

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(flow1: Flow<T>, flow2: Flow<T>): Flow<T> = flow {
  emitAll(flow1)
  emitAll(flow2)
}

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(flow1: Flow<T>, flow2: Flow<T>, flow3: Flow<T>): Flow<T> = flow {
  emitAll(flow1)
  emitAll(flow2)
  emitAll(flow3)
}

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>,
): Flow<T> =
  flow {
    emitAll(flow1)
    emitAll(flow2)
    emitAll(flow3)
    emitAll(flow4)
  }

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>,
  flow5: Flow<T>,
): Flow<T> =
  flow {
    emitAll(flow1)
    emitAll(flow2)
    emitAll(flow3)
    emitAll(flow4)
    emitAll(flow5)
  }

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(flow1: Flow<T>, flow2: Flow<T>, vararg flows: Flow<T>): Flow<T> = flow {
  emitAll(flow1)
  emitAll(flow2)
  flows.forEach {
    emitAll(it)
  }
}

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(flows: Iterable<Flow<T>>): Flow<T> {
  return flow { flows.forEach { emitAll(it) } }
}

/**
 * Creates an output [Flow] which sequentially emits all values from the first given [Flow] and then moves on to the next.
 */
public fun <T> concat(flows: Sequence<Flow<T>>): Flow<T> {
  return flow { flows.forEach { emitAll(it) } }
}

//
// concatWith
//

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(flow: Flow<T>): Flow<T> = concat(this, flow)

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(flow1: Flow<T>, flow2: Flow<T>): Flow<T> =
  concat(this, flow1, flow2)

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(flow1: Flow<T>, flow2: Flow<T>, flow3: Flow<T>): Flow<T> =
  concat(this, flow1, flow2, flow3)

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(
  flow1: Flow<T>,
  flow2: Flow<T>,
  flow3: Flow<T>,
  flow4: Flow<T>,
): Flow<T> =
  concat(this, flow1, flow2, flow3, flow4)

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(flow: Flow<T>, vararg others: Flow<T>): Flow<T> =
  concat(this, flow, *others)

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(others: Iterable<Flow<T>>): Flow<T> {
  return flow {
    emitAll(this@concatWith)
    others.forEach { emitAll(it) }
  }
}

/**
 * Returns a [Flow] that emits the items emitted from the current [Flow], then the next, one after the other, without interleaving them.
 */
public fun <T> Flow<T>.concatWith(others: Sequence<Flow<T>>): Flow<T> {
  return flow {
    emitAll(this@concatWith)
    others.forEach { emitAll(it) }
  }
}

/**
 * This function is an alias to [concatWith] operator.
 *
 * Returns a [Flow] that emits the items emitted from this [Flow], then the next, one after the other, without interleaving them.
 *
 * Example:
 * ``` kotlin
 * val flow1 = flowOf(1, 2, 3)
 * val flow2 = flowOf(4, 5, 6)
 * val result = flow1 + flow2 // produces the following emissions 1, 2, 3, 4, 5, 6
 * ```
 *
 * @see concatWith
 */
public operator fun <T> Flow<T>.plus(other: Flow<T>): Flow<T> = concat(this, other)

//
// startWith
//

/**
 * Returns a [Flow] that emits a specified item before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item: T): Flow<T> = concat(flowOf(item), this)

/**
 * Returns a [Flow] that emits a specified item before it begins to emit items emitted by the current [Flow].
 * [itemFactory] will be called on the collection for each new [FlowCollector].
 */
public fun <T> Flow<T>.startWith(itemFactory: suspend () -> T): Flow<T> = concat(
  flow { emit(itemFactory()) },
  this,
)

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item1: T, item2: T): Flow<T> = concat(
  flow {
    emit(item1)
    emit(item2)
  },
  this,
)

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item1: T, item2: T, item3: T): Flow<T> = concat(
  flow {
    emit(item1)
    emit(item2)
    emit(item3)
  },
  this,
)

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item1: T, item2: T, item3: T, item4: T): Flow<T> = concat(
  flow {
    emit(item1)
    emit(item2)
    emit(item3)
    emit(item4)
  },
  this,
)

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item1: T, item2: T, item3: T, item4: T, item5: T): Flow<T> =
  concat(
    flow {
      emit(item1)
      emit(item2)
      emit(item3)
      emit(item4)
      emit(item5)
    },
    this,
  )

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(item: T, vararg items: T): Flow<T> {
  return concat(
    flow {
      emit(item)
      items.forEach {
        emit(it)
      }
    },
    this,
  )
}

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(others: Iterable<T>): Flow<T> = concat(others.asFlow(), this)

/**
 * Returns a [Flow] that emits the specified items before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(others: Sequence<T>): Flow<T> = concat(others.asFlow(), this)

/**
 * Returns a [Flow] that emits the items in a specified [Flow] before it begins to emit items emitted by the current [Flow].
 */
public fun <T> Flow<T>.startWith(other: Flow<T>): Flow<T> = concat(other, this)
