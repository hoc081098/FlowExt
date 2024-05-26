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

/**
 * Concurrent map implementation. Please do not use it.
 */
@Suppress("unused")
internal actual class ConcurrentHashMap<K : Any, V : Any> internal actual constructor() : MutableMap<K, V> {
  private val delegate = LinkedHashMap<K, V>()
  private val lock = Lock()

  actual override val size: Int get() = delegate.size

  actual override fun containsKey(key: K): Boolean =
    synchronized(lock) { delegate.containsKey(key) }

  actual override fun containsValue(value: V): Boolean =
    synchronized(lock) { delegate.containsValue(value) }

  actual override fun get(key: K): V? = synchronized(lock) { delegate[key] }

  actual override fun isEmpty(): Boolean = delegate.isEmpty()

  actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = synchronized(lock) { delegate.entries }

  actual override val keys: MutableSet<K>
    get() = synchronized(lock) { delegate.keys }

  actual override val values: MutableCollection<V>
    get() = synchronized(lock) { delegate.values }

  actual override fun clear() = synchronized(lock, delegate::clear)

  actual override fun put(
    key: K,
    value: V,
  ): V? = synchronized(lock) { delegate.put(key, value) }

  actual override fun putAll(from: Map<out K, V>) = synchronized(lock) { delegate.putAll(from) }

  actual override fun remove(key: K): V? = synchronized(lock) { delegate.remove(key) }

  override fun hashCode(): Int = synchronized(lock, delegate::hashCode)

  override fun equals(other: Any?): Boolean =
    synchronized(lock) {
      if (other !is Map<*, *>) return false
      return other == delegate
    }

  override fun toString(): String = "ConcurrentHashMap.native by $delegate"
}
