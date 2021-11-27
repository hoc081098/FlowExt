package com.hoc081098.flowext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class RetryWhenWithDelayStrategyTest {

  @Test
  fun test() = suspendTest {
    var count = 0
    val retries = 3
    val flow = flow {
      emit(1)
      throw RuntimeException((count++).toString())
    }

    val sum =
      flow.retryWhenWithDelayStrategy(DelayStrategy.Constant(100.milliseconds)) { cause, attempt ->
        assertEquals(
          attempt.toString(),
          assertIs<RuntimeException>(cause).message,
        )
        attempt < retries
      }.catch { cause ->
        assertEquals(
          retries.toString(),
          assertIs<RuntimeException>(cause).message,
        )
      }.fold(0, Int::plus)
    assertEquals(4, sum)
  }
}

class DelayStrategyTest {
  private val cause = RuntimeException()
  private val attempt = 0L

  @Test
  fun testImmediate() {
    repeat(10) {
      assertEquals(
        Duration.ZERO,
        DelayStrategy.Immediate.duration(cause, attempt)
      )
    }
  }

  @Test
  fun testConstant() {
    val duration = 100.milliseconds

    repeat(10) {
      assertEquals(
        duration,
        DelayStrategy.Constant(duration).duration(cause, attempt)
      )
    }
  }

  @Test
  fun testExponential() {
    fun every(initialDelay: Duration, factor: Double, maxDelay: Duration) {
      val strategy = DelayStrategy.Exponential(
        initialDelay = initialDelay,
        factor = factor,
        maxDelay = maxDelay,
      )

      fun Duration.coerce(): Duration = coerceAtMost(maxDelay)

      assertEquals(
        initialDelay.coerce(),
        strategy.duration(cause, 0),
      )
      assertEquals(
        (initialDelay * factor).coerce(),
        strategy.duration(cause, 1),
      )
      assertEquals(
        (initialDelay * factor * factor).coerce(),
        strategy.duration(cause, 2),
      )
      assertEquals(
        (initialDelay * factor * factor * factor).coerce(),
        strategy.duration(cause, 3),
      )
      assertEquals(
        (initialDelay * factor * factor * factor * factor).coerce(),
        strategy.duration(cause, 4),
      )
      assertEquals(
        (initialDelay * factor * factor * factor * factor * factor).coerce(),
        strategy.duration(cause, 5),
      )
      assertEquals(
        (initialDelay * factor.pow(100)).coerce(),
        strategy.duration(cause, 100),
      )
    }

    listOf<Triple<Duration, Double, Duration>>(
      Triple(100.milliseconds, 1.0, 2_000.milliseconds),
      Triple(100.milliseconds, 1.5, 2_000.milliseconds),
      Triple(100.milliseconds, 2.0, 2_000.milliseconds),
      Triple(100.milliseconds, 2.7, 2_000.milliseconds),
      Triple(100.milliseconds, 3.69, 2_000.milliseconds),
      Triple(100.milliseconds, 100.0, 2_000.milliseconds),
      //
      Triple(10.milliseconds, 2.0, 1_000.milliseconds),
      Triple(20.milliseconds, 2.0, 1_000.milliseconds),
      Triple(30.milliseconds, 2.0, 1_000.milliseconds),
      Triple(40.milliseconds, 2.0, 1_000.milliseconds),
      Triple(50.milliseconds, 2.0, 1_000.milliseconds),
      Triple(23823.milliseconds, 2.0, 1_000.milliseconds),
      //
      Triple(100.milliseconds, 1.5, 1_000.milliseconds),
      Triple(100.milliseconds, 1.5, 2_000.milliseconds),
      Triple(100.milliseconds, 1.5, 3_000.milliseconds),
      Triple(100.milliseconds, 1.5, 4_000.milliseconds),
      Triple(100.milliseconds, 1.5, 5_000.milliseconds),
      Triple(100.milliseconds, 1.5, 6_000.milliseconds),
    ).forEach { (a, b, c) -> every(a, b, c) }
  }
}
