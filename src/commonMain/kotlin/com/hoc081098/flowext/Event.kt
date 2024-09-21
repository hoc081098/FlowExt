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

@file:Suppress("NOTHING_TO_INLINE")

package com.hoc081098.flowext

import dev.drewhamilton.poko.Poko

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
  @Poko
  public class Value<out T>(public val value: T) : Event<T> {
    override fun toString(): String = "Event.Value($value)"
  }

  /**
   * A error event containing the [error].
   */
  @Poko
  public class Error(public val error: Throwable) : Event<Nothing> {
    override fun toString(): String = "Event.Error($error)"
  }

  /**
   * Complete event.
   */
  public data object Complete : Event<Nothing> {
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
 * Returns the result of applying [transform] to this [Event]'s [value][Event.Value.value] if this is a [Event.Value].
 * Otherwise, returns itself.
 *
 * Slightly different from [map] in that [transform] is expected to
 * return an [Event] (which could be [Event.Error] or [Event.Complete]).
 */
public inline fun <T, R> Event<T>.flatMap(transform: (T) -> Event<R>): Event<R> = when (this) {
  Event.Complete -> Event.Complete
  is Event.Error -> this
  is Event.Value -> transform(value)
}

/**
 * Returns the encapsulated [value][Event.Value.value] if this [Event] is a [Event.Value], otherwise returns `null`.
 */
public inline fun <T> Event<T>.valueOrNull(): T? = valueOrElse { null }

/**
 * Returns the encapsulated [value][Event.Value.value] if this [Event] is a [Event.Value], otherwise returns [defaultValue].
 */
public inline fun <T> Event<T>.valueOrDefault(defaultValue: T): T? = valueOrElse { defaultValue }

/**
 * Returns the encapsulated value if this [Event] is a [Event.Value].
 * If this is [Event.Error], throws the encapsulated [error][Event.Error.error].
 * Otherwise, throws a [NoSuchElementException].
 */
public inline fun <T> Event<T>.valueOrThrow(): T =
  valueOrElse { throw it ?: NoSuchElementException("$this has no value!") }

/**
 * Returns the encapsulated [value][Event.Value.value] if this [Event] is a [Event.Value],
 * otherwise returns the result of calling [defaultValue] function.
 *
 * The function [defaultValue] will be called with `null` if this [Event] is [Event.Complete].
 */
public inline fun <T> Event<T>.valueOrElse(defaultValue: (Throwable?) -> T): T = when (this) {
  Event.Complete -> defaultValue(null)
  is Event.Error -> defaultValue(error)
  is Event.Value -> value
}

/**
 * Returns the encapsulated [error][Event.Error.error] if this [Event] is an [Event.Error], otherwise returns `null`.
 */
public inline fun <T> Event<T>.errorOrNull(): Throwable? = when (this) {
  Event.Complete -> null
  is Event.Error -> error
  is Event.Value -> null
}

/**
 * Returns the encapsulated [error][Event.Error.error] if this [Event] is an [Event.Error].
 * Otherwise, throws a [NoSuchElementException].
 */
public inline fun <T> Event<T>.errorOrThrow(): Throwable =
  errorOrNull() ?: throw NoSuchElementException("$this has no error!")
