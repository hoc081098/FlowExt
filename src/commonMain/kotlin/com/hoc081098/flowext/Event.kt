/*
 * MIT License
 *
 * Copyright (c) 2021 Petrus Nguyễn Thái Học
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

/**
 * Represents the reactive signal types: `value`, `error` and `complete`,
 * and holds their parameter values (a value, a [Throwable], nothing).
 *
 * Sequence grammar:
 * `value* (error | complete)`
 */
public sealed interface Event<out T> {
  /**
   * A value event containing the given [value].
   */
  public data class Value<out T>(public val value: T) : Event<T> {
    override fun toString(): String = "Event.Value($value)"
  }

  /**
   * A error event containing the [error].
   */
  public data class Error(public val error: Throwable) : Event<Nothing> {
    override fun toString(): String = "Event.Error($error)"
  }

  /**
   * Complete event.
   */
  public object Complete : Event<Nothing> {
    override fun toString(): String = "Event.Complete"
  }
}

/**
 * When this [Event] is a [Event.Value], return a [Event.Value] containing transformed value using [transform].
 * Otherwise, returns itself.
 */
public inline fun <T, R> Event<T>.map(transform: (T) -> R): Event<R> = when (this) {
  Event.Complete -> Event.Complete
  is Event.Error -> this
  is Event.Value -> Event.Value(transform(value))
}

/**
 * Returns the encapsulated [value][Event.Value.value] if this [Event] is a [Event.Value], otherwise returns `null`.
 */
public fun <T> Event<T>.valueOrNull(): T? = when (this) {
  Event.Complete -> null
  is Event.Error -> null
  is Event.Value -> value
}

/**
 * Returns the encapsulated value if this [Event] is a [Event.Value].
 * If this is [Event.Error], throws the encapsulated [error][Event.Error.error].
 * Otherwise, throws a [NoSuchElementException].
 */
public fun <T> Event<T>.valueOrThrow(): T = when (this) {
  Event.Complete -> throw NoSuchElementException("$this has no value!")
  is Event.Error -> throw error
  is Event.Value -> value
}

/**
 * Returns the encapsulated [error][Event.Error.error] if this [Event] is an [Event.Error], otherwise returns `null`.
 */
public fun <T> Event<T>.errorOrNull(): Throwable? = when (this) {
  Event.Complete -> null
  is Event.Error -> error
  is Event.Value -> null
}

/**
 * Returns the encapsulated [error][Event.Error.error] if this [Event] is an [Event.Error].
 * Otherwise, throws a [NoSuchElementException].
 */
public fun <T> Event<T>.errorOrThrow(): Throwable = when (this) {
  Event.Complete -> throw NoSuchElementException("$this has no error!")
  is Event.Error -> error
  is Event.Value -> throw NoSuchElementException("$this has no error!")
}
