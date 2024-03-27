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

package com.hoc081098.flowext

import com.hoc081098.flowext.utils.TestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class EventTest {
  @Test
  fun valueEventToString() {
    assertEquals("Event.Value(1)", Event.Value(1).toString())
    assertEquals("Event.Value(hoc081098)", Event.Value("hoc081098").toString())
  }

  @Test
  fun valueEventEqualsAndHashCode() {
    assertEquals(
      Event.Value(listOf(1, 2, 3)),
      Event.Value(listOf(1, 2, 3)),
    )
    assertEquals(
      Event.Value(listOf(1, 2, 3)).hashCode(),
      Event.Value(listOf(1, 2, 3)).hashCode(),
    )
    assertEquals(
      listOf(1, 2, 3).hashCode(),
      Event.Value(listOf(1, 2, 3)).hashCode(),
    )
  }

  @Test
  fun errorEventToString() {
    val error = TestException()
    assertEquals(
      "Event.Error($error)",
      Event.Error(error).toString(),
    )
  }

  @Test
  fun errorEventEqualsAndHashCode() {
    val e = TestException()
    assertEquals(
      Event.Error(e),
      Event.Error(e),
    )
    assertEquals(
      Event.Error(e).hashCode(),
      Event.Error(e).hashCode(),
    )
    assertEquals(
      e.hashCode(),
      Event.Error(e).hashCode(),
    )
  }

  @Test
  fun completeEventToString() {
    assertEquals("Event.Complete", Event.Complete.toString())
  }

  @Test
  fun completeEventEqualsAndHashCode() {
    assertEquals(
      Event.Complete,
      Event.Complete,
    )
    assertSame(
      Event.Complete,
      Event.Complete,
    )
    assertEquals(
      Event.Complete.hashCode(),
      Event.Complete.hashCode(),
    )
  }

  @Test
  fun eventMap() {
    assertEquals(
      Event.Value(2),
      Event.Value(1).map { it + 1 },
    )
    assertEquals(
      "throws",
      assertFailsWith<TestException> {
        Event.Value(1).map { throw TestException("throws") }
      }.message,
    )

    val errorEvent: Event<Int> = Event.Error(TestException("1"))
    assertSame(
      errorEvent,
      errorEvent.map { it + 1 },
    )

    val completeEvent: Event<Int> = Event.Complete
    assertSame(
      completeEvent,
      completeEvent.map { it + 1 },
    )
  }

  @Test
  fun eventFlatmap() {
    assertEquals(
      Event.Value(2),
      Event.Value(1).flatMap { Event.Value(it + 1) },
    )
    assertSame(
      Event.Complete,
      Event.Value(1).flatMap { Event.Complete },
    )
    val error = TestException()
    assertEquals(
      Event.Error(error),
      Event.Value(1).flatMap { Event.Error(error) },
    )
    assertEquals(
      "throws",
      assertFailsWith<TestException> {
        Event.Value(1).flatMap<Int, String> { throw TestException("throws") }
      }.message,
    )

    val errorEvent: Event<Int> = Event.Error(TestException("1"))
    assertSame(
      errorEvent,
      errorEvent.flatMap { Event.Value(it + 1) },
    )

    val completeEvent: Event<Int> = Event.Complete
    assertSame(
      completeEvent,
      completeEvent.flatMap { Event.Value(it + 1) },
    )
  }

  @Test
  fun valueOrNull() {
    listOf(
      Event.Value(1) to 1,
      Event.Error(TestException()) to null,
      Event.Complete to null,
    ).forEach { (e, v) ->
      assertEquals(v, e.valueOrNull())
    }
  }

  @Test
  fun valueOrDefault() {
    val defaultValue = 2

    listOf(
      Event.Value(1) to 1,
      Event.Error(TestException()) to defaultValue,
      Event.Complete to defaultValue,
    ).forEach { (e, v) ->
      assertEquals(v, e.valueOrDefault(defaultValue))
    }
  }

  @Test
  fun valueOrElse() {
    assertEquals(1, Event.Value(1).valueOrElse { error("Should not reach here!") })

    assertEquals(
      2,
      Event.Error(TestException("1")).valueOrElse {
        assertIs<TestException>(assertNotNull(it))
        2
      },
    )
    assertEquals(
      2,
      Event.Complete.valueOrElse {
        assertNull(it)
        2
      },
    )
  }

  @Test
  fun valueOrThrow() {
    assertEquals(1, Event.Value(1).valueOrThrow())
    assertEquals(
      "1",
      assertFailsWith<TestException> { Event.Error(TestException("1")).valueOrThrow() }.message,
    )
    assertEquals(
      "Event.Complete has no value!",
      assertFailsWith<NoSuchElementException> { Event.Complete.valueOrThrow() }.message,
    )
  }

  @Test
  fun errorOrNull() {
    val exception = TestException()
    listOf(
      Event.Value(1) to null,
      Event.Error(exception) to exception,
      Event.Complete to null,
    ).forEach { (e, v) ->
      assertEquals(v, e.errorOrNull())
    }
  }

  @Test
  fun errorOrThrow() {
    assertEquals(
      "Event.Value(1) has no error!",
      assertFailsWith<NoSuchElementException> { Event.Value(1).errorOrThrow() }.message,
    )

    val e = TestException()
    assertSame(e, Event.Error(e).errorOrThrow())
    assertEquals(
      "Event.Complete has no error!",
      assertFailsWith<NoSuchElementException> { Event.Complete.errorOrThrow() }.message,
    )
  }
}
