
import com.hoc081098.flowext.suspendTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
        "a" to 1,
        "b" to 2,
        "c" to 3,
        "d" to 4,
        "e" to 4,
      )
    )
  }
}
