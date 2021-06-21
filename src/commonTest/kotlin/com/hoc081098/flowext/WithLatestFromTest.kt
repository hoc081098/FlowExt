
import com.hoc081098.flowext.suspendTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

@ExperimentalTime
@ExperimentalCoroutinesApi
class WithLatestFromTest {
  @Test
  fun basic() = suspendTest {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to 4,
        "b" to 4,
        "c" to 4,
        "d" to 4,
        "e" to 4,
      )
    )
  }

  @Test
  fun basicWithNull() = suspendTest {
    val f1 = flowOf(1, 2, 3, 4, null)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to null,
        "b" to null,
        "c" to null,
        "d" to null,
        "e" to null,
      )
    )
  }

  @Test
  fun basic2() = suspendTest {
    val f1 = flowOf(1, 2, 3, 4).onEach { delay(300) }
    val f2 = flowOf("a", "b", "c", "d", "e").onEach { delay(100) }
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "c" to 1,
        "d" to 1,
        "e" to 1,
      )
    )
  }
}
