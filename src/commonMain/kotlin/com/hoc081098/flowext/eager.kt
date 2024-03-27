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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.launch

/**
 * Returns a flow containing the results of applying the given [transform] function.
 * Transformations performed parallel with given [concurrency] limit and preserving the order of elements.
 */
@FlowPreview // TODO: related issue https://github.com/Kotlin/kotlinx.coroutines/issues/3938
@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.mapEager(
  concurrency: Int = DEFAULT_CONCURRENCY,
  transform: suspend (value: T) -> R,
): Flow<R> = if (concurrency == 1) {
  map(transform)
} else {
  map { value -> flow { emit(transform(value)) } }.flattenConcatEager(concurrency, bufferSize = Channel.RENDEZVOUS)
}

/**
 * Transforms elements emitted by the original flow by applying [transform], that returns another flow,
 * and then merging and flattening these flows.
 *
 * This operator calls [transform] *sequentially* and then concatenates the resulting flows with a [concurrency]
 * limit on the number of concurrently collected flows.
 * It is a shortcut for `map(transform).flattenConcatEager(concurrency)`.
 * See [flattenConcatEager] for details.
 *
 * ### Operator fusion
 *
 * Applications of [flowOn], [buffer], and [produceIn] _after_ this operator are fused with
 * its concurrent merging so that only one properly configured channel is used for execution of merging logic.
 *
 * @param concurrency controls the number of in-flight flows, at most [concurrency] flows are collected
 * at the same time. By default, it is equal to [DEFAULT_CONCURRENCY].
 * @param bufferSize hints about the number of expected values from each inner [Flow]. Should be either
 * a positive channel capacity or one of the constants defined in [Channel]. If inner [Flow] tries to emit
 * more than [bufferSize] values before being concatenated to output, then it will be suspended
 */
@FlowPreview // TODO: related issue https://github.com/Kotlin/kotlinx.coroutines/issues/3938
@ExperimentalCoroutinesApi
public fun <T, R> Flow<T>.flatMapConcatEager(
  concurrency: Int = DEFAULT_CONCURRENCY,
  bufferSize: Int = Channel.BUFFERED,
  transform: suspend (value: T) -> Flow<R>,
): Flow<R> = map(transform).flattenConcatEager(concurrency, bufferSize)

/**
 * Flattens the given flow of flows into a single flow in a sequential manner, without interleaving nested flows.
 * But unlike [flattenConcat] collecting nested flows performed concurrently with a given [concurrency] limit
 * on the number of concurrently collected flows.
 *
 * If [concurrency] is more than 1, then inner flows are collected by this operator *concurrently*.
 * With `concurrency == 1` this operator is identical to [flattenConcat].
 *
 * ### Operator fusion
 *
 * Applications of [flowOn], [buffer], and [produceIn] _after_ this operator are fused with
 * its concurrent merging so that only one properly configured channel is used for execution of merging logic.
 *
 * When [concurrency] is greater than 1, this operator is [buffered][buffer] by default
 * and size of its output buffer can be changed by applying subsequent [buffer] operator.
 *
 * @param concurrency controls the number of in-flight flows, at most [concurrency] flows are collected
 * at the same time. By default, it is equal to [DEFAULT_CONCURRENCY].
 * @param bufferSize hints about the number of expected values from each inner [Flow]. Should be either
 * a positive channel capacity or one of the constants defined in [Channel]. If [Flow] tries to emit
 * more than [bufferSize] values before being concatenated to output, then it will be suspended
 */
@FlowPreview // TODO: related issue https://github.com/Kotlin/kotlinx.coroutines/issues/3938
@ExperimentalCoroutinesApi
public fun <T> Flow<Flow<T>>.flattenConcatEager(
  concurrency: Int = DEFAULT_CONCURRENCY,
  bufferSize: Int = Channel.BUFFERED,
): Flow<T> {
  require(concurrency > 0) { "Expected positive concurrency level, but had $concurrency" }
  return if (concurrency == 1) flattenConcat() else flattenConcatEagerInternal(this, concurrency, bufferSize)
}

private fun <T> flattenConcatEagerInternal(flow: Flow<Flow<T>>, concurrency: Int, bufferSize: Int) = channelFlow {
  val collectors = Channel<ProxyCollector<T>>(concurrency - 1)
  val job: Job? = coroutineContext[Job]
  launch {
    job?.ensureActive()
    collectors.consumeEach { collector ->
      collector.collectTo(this@channelFlow)
    }
  }
  flow
    .onCompletion { collectors.close() }
    .collect { inner ->
      job?.ensureActive()
      val collector = ProxyCollector(Channel<T>(bufferSize))
      collectors.send(collector)
      launch {
        try {
          inner.collect(collector)
        } finally {
          collector.close()
        }
      }
    }
}

private class ProxyCollector<T>(private val channel: Channel<T>) : FlowCollector<T> {
  override suspend fun emit(value: T) {
    channel.send(value)
  }

  suspend fun collectTo(scope: ProducerScope<T>) {
    channel.consumeEach { elem ->
      scope.send(elem)
    }
  }

  fun close() {
    channel.close()
  }
}
