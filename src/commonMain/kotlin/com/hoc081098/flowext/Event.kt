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
