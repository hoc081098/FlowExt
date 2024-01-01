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
internal actual class ConcurrentHashMap<K, V> internal actual constructor() : MutableMap<K, V> {
  private val delegate = LinkedHashMap<K, V>()
  private val lock = Lock()

  override val size: Int get() = delegate.size

  override fun containsKey(key: K): Boolean = synchronized(lock) { delegate.containsKey(key) }

  override fun containsValue(value: V): Boolean = synchronized(lock) { delegate.containsValue(value) }

  override fun get(key: K): V? = synchronized(lock) { delegate[key] }

  override fun isEmpty(): Boolean = delegate.isEmpty()

  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = synchronized(lock) { delegate.entries }

  override val keys: MutableSet<K>
    get() = synchronized(lock) { delegate.keys }

  override val values: MutableCollection<V>
    get() = synchronized(lock) { delegate.values }

  override fun clear() = synchronized(lock, delegate::clear)

  override fun put(
    key: K,
    value: V,
  ): V? = synchronized(lock) { delegate.put(key, value) }

  override fun putAll(from: Map<out K, V>) = synchronized(lock) { delegate.putAll(from) }

  override fun remove(key: K): V? = synchronized(lock) { delegate.remove(key) }

  override fun hashCode(): Int = synchronized(lock, delegate::hashCode)

  override fun equals(other: Any?): Boolean =
    synchronized(lock) {
      if (other !is Map<*, *>) return false
      return other == delegate
    }

  override fun toString(): String = "ConcurrentHashMap.native by $delegate"
}
