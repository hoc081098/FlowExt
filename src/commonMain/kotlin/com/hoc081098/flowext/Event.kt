package com.hoc081098.flowext

public sealed interface Event<out T> {
  public data class Value<out T>(public val value: T) : Event<T> {
    override fun toString(): String = "Event.Value($value)"
  }

  public data class Error(public val throwable: Throwable) : Event<Nothing> {
    override fun toString(): String = "Event.Error($throwable)"
  }

  public object Complete : Event<Nothing> {
    override fun toString(): String = "Event.Complete"
  }
}

public fun <T, R> Event<T>.map(transform: (T) -> R): Event<R> = when (this) {
  Event.Complete -> Event.Complete
  is Event.Error -> this
  is Event.Value -> Event.Value(transform(value))
}

public fun <T> Event<T>.valueOrNull(): T? = when (this) {
  Event.Complete -> null
  is Event.Error -> null
  is Event.Value -> value
}

public fun <T> Event<T>.valueOrThrow(): T = when (this) {
  Event.Complete -> throw NoSuchElementException("$this has no value!")
  is Event.Error -> throw throwable
  is Event.Value -> value
}

public fun <T> Event<T>.throwableOrNull(): Throwable? = when (this) {
  Event.Complete -> null
  is Event.Error -> throwable
  is Event.Value -> null
}

public fun <T> Event<T>.throwableOrThrow(): Throwable = when (this) {
  Event.Complete -> throw NoSuchElementException("$this has no throwable!")
  is Event.Error -> throwable
  is Event.Value -> throw NoSuchElementException("$this has no throwable!")
}
