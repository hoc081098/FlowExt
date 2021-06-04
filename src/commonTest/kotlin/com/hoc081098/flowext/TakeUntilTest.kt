package com.hoc081098.flowext

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class TakeUntilTest {
    @Test
    fun sourceCompletesAfterNotifier() = suspendTest {
        range(0, 10)
            .onEach { delay(30) }
            .takeUntil(
                timer(
                    Unit,
                    Duration.milliseconds(110)
                )
            )
            .test {
                assertEquals(0, expectItem())
                assertEquals(1, expectItem())
                assertEquals(2, expectItem())
                expectComplete()
            }
    }

    @Test
    fun sourceCompletesBeforeNotifier() = suspendTest {
        range(0, 10)
            .onEach { delay(30) }
            .takeUntil(
                timer(
                    Unit,
                    Duration.seconds(10)
                )
            )
            .test {
                (0 until 10).forEach {
                    assertEquals(it, expectItem())
                }
                expectComplete()
            }
    }
}