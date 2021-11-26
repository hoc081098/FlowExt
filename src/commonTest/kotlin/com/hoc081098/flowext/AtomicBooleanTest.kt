package com.hoc081098.flowext

import com.hoc081098.flowext.internal.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AtomicBooleanTest {
  @Test
  fun default_initial_value_is_false() {
    assertFalse(AtomicBoolean().value)
    assertTrue(AtomicBoolean(true).value)
  }

  @Test
  fun compareAndSet_success_from_false_to_true() {
    val ref = AtomicBoolean(false)
    val result = ref.compareAndSet(expect = false, update = true)
    assertTrue(result)
    assertTrue(ref.value)
  }

  @Test
  fun compareAndSet_fail_from_false_to_true() {
    val ref = AtomicBoolean(false)
    val result = ref.compareAndSet(expect = true, update = true)
    assertFalse(result)
    assertFalse(ref.value)
  }

  @Test
  fun compareAndSet_success_from_true_to_false() {
    val ref = AtomicBoolean(true)
    val result = ref.compareAndSet(expect = true, update = false)
    assertTrue(result)
    assertFalse(ref.value)
  }

  @Test
  fun compareAndSet_fail_from_true_to_false() {
    val ref = AtomicBoolean(true)
    val result = ref.compareAndSet(expect = false, update = false)
    assertFalse(result)
    assertTrue(ref.value)
  }
}
