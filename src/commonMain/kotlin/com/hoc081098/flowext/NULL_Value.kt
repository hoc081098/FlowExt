package com.hoc081098.flowext

/**
 * This is a work-around for having nested nulls in generic code.
 * This allows for writing faster generic code instead of using `Option`.
 * This is only used as an optimisation technique in low-level code.
 */
@Suppress("ClassName")
public object NULL_Value {
  @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
  public inline fun <T> unbox(v: Any?): T = if (this === v) null as T else v as T
}
