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

package com.hoc081098.flowext.internal

internal actual class ConcurrentHashMap<K, V> actual constructor() : AbstractMap<K, V>(), MutableMap<K, V> {
  override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    get() = TODO("Not yet implemented")
  override val keys: MutableSet<K>
    get() = TODO("Not yet implemented")
  override val values: MutableCollection<V>
    get() = TODO("Not yet implemented")

  override fun clear() {
    TODO("Not yet implemented")
  }

  override fun remove(key: K): V? {
    TODO("Not yet implemented")
  }

  override fun putAll(from: Map<out K, V>) {
    TODO("Not yet implemented")
  }

  override fun put(key: K, value: V): V? {
    TODO("Not yet implemented")
  }
}
