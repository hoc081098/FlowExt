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

package com.hoc081098.flowext.internal

internal actual class ConcurrentHashMap<K : Any, V : Any> internal actual constructor() :
  MutableMap<K, V> {
    private val delegate = java.util.concurrent.ConcurrentHashMap<K, V>()

    actual override val size: Int
      get() = delegate.size

    actual override fun containsKey(key: K): Boolean = delegate.containsKey(key)

    actual override fun containsValue(value: V): Boolean = delegate.containsValue(value)

    actual override fun get(key: K): V? = delegate[key]

    actual override fun isEmpty(): Boolean = delegate.isEmpty()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
      get() = delegate.entries

    actual override val keys: MutableSet<K>
      get() = delegate.keys

    actual override val values: MutableCollection<V>
      get() = delegate.values

    actual override fun clear() = delegate.clear()

    actual override fun put(key: K, value: V): V? = delegate.put(key, value)

    actual override fun putAll(from: Map<out K, V>) = delegate.putAll(from)

    actual override fun remove(key: K): V? = delegate.remove(key)

    override fun hashCode(): Int = delegate.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is Map<*, *>) return false
      return other == delegate
    }

    override fun toString(): String = "ConcurrentHashMap.jvm by $delegate"
  }
