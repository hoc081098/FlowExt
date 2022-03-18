/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
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
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield

@ExperimentalCoroutinesApi
public fun <T> race(flow: Flow<T>, vararg flows: Flow<T>): Flow<T> =
  if (flows.isEmpty()) race(listOf(flow))
  else race(listOf(flow) + flows)

@ExperimentalCoroutinesApi
public fun <T> race(flows: Iterable<Flow<T>>): Flow<T> = flow {
  coroutineScope {
    val channels = flows.map { flow ->
      // Produce the values using the default (rendezvous) channel
      produce {
        flow.collect {
          send(it)
          yield() // Emulate fairness, giving each flow chance to emit
        }
      }
    }

    if (channels.isEmpty()) {
      return@coroutineScope
    }
    channels
      .singleOrNull()
      ?.let { return@coroutineScope emitAll(it) }

    val (winnerIndex, winnerResult) = select<Pair<Int, ChannelResult<T>>> {
      channels.forEachIndexed { index, channel ->
        channel.onReceiveCatching {
          index to it
        }
      }
    }
    println("winnerIndex=$winnerIndex, winnerResult=$winnerResult")

    channels.forEachIndexed { index, channel ->
      if (index != winnerIndex) {
        channel.cancel()
      }
    }

    winnerResult
      .onSuccess {
        emit(it)
        emitAll(channels[winnerIndex])
      }
      .onFailure {
        println("onFailure $it")
        it?.let { throw it }
      }
  }
}
