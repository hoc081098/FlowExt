/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
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

@file:Suppress("UNCHECKED_CAST", "ReplaceManualRangeWithIndicesCalls")

package com.hoc081098.flowext

import com.hoc081098.flowext.utils.NULL_VALUE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private inline fun <T, R> Array<T>.mapArray(transform: (T) -> R): Array<R> {
  val result = arrayOfNulls<Any?>(size)
  for (i in 0 until size) {
    result[i] = transform(this[i])
  }
  return result as Array<R>
}

private fun <State, SubState, Result> Flow<State>.selectInternal(
  selectors: Array<suspend (State) -> SubState>,
  projector: suspend (Array<SubState>) -> Result
): Flow<Result> {
  check(selectors.isNotEmpty()) { "selectors must not be empty" }

  return flow {
    var latestSubStates: Array<Any?>? = null // Array<SubState>?
    var latestState: Any? = NULL_VALUE // Result | NULL_VALUE
    var reusableSubStates: Array<Any?>? = null

    collect { state ->
      val currentSubStates = reusableSubStates
        ?: arrayOfNulls<Any?>(selectors.size).also { reusableSubStates = it }

      for (i in 0 until selectors.size) {
        currentSubStates[i] = selectors[i](state)
      }

      if (latestSubStates === null || !currentSubStates.contentEquals(latestSubStates)) {
        val currentState = projector(
          (currentSubStates)
            .copyOf()
            .also { latestSubStates = it } as Array<SubState>
        )

        if (latestState === NULL_VALUE || (latestState as Result) != currentState) {
          latestState = currentState
          emit(currentState)
        }
      }
    }
  }
}

private fun <State, SubState, Result> StateFlow<State>.selectStateInternal(
  scope: CoroutineScope,
  started: SharingStarted,
  selectors: Array<(State) -> SubState>,
  projector: (Array<SubState>) -> Result
): StateFlow<Result> {
  return selectInternal(
    selectors = selectors.mapArray { selector ->
      val f: suspend (State) -> SubState = { selector(it) }
      f
    },
    projector = projector
  ).stateIn(
    scope = scope,
    started = started,
    initialValue = projector(selectors.mapArray { it(value) })
  )
}

// -------------------------------------------------------------------------------------------------

public fun <State, Result> Flow<State>.select(selector: (State) -> Result): Flow<Result> =
  map(selector).distinctUntilChanged()

public fun <State, SubState1, SubState2, Result> Flow<State>.select2(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  projector: suspend (subState1: SubState1, subState2: SubState2) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(selector1, selector2),
  projector = { projector(it[0] as SubState1, it[1] as SubState2) }
)

public fun <State, SubState1, SubState2, SubState3, Result> Flow<State>.select3(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, Result> Flow<State>.select4(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, Result> Flow<State>.select5(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  selector5: suspend (State) -> SubState5,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4, subState5: SubState5) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, Result> Flow<State>.select6(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  selector5: suspend (State) -> SubState5,
  selector6: suspend (State) -> SubState6,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4, subState5: SubState5, subState6: SubState6) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, Result> Flow<State>.select7(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  selector5: suspend (State) -> SubState5,
  selector6: suspend (State) -> SubState6,
  selector7: suspend (State) -> SubState7,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4, subState5: SubState5, subState6: SubState6, subState7: SubState7) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, SubState8, Result> Flow<State>.select8(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  selector5: suspend (State) -> SubState5,
  selector6: suspend (State) -> SubState6,
  selector7: suspend (State) -> SubState7,
  selector8: suspend (State) -> SubState8,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4, subState5: SubState5, subState6: SubState6, subState7: SubState7, subState8: SubState8) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7,
    selector8
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7,
      it[7] as SubState8
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, SubState8, SubState9, Result> Flow<State>.select9(
  selector1: suspend (State) -> SubState1,
  selector2: suspend (State) -> SubState2,
  selector3: suspend (State) -> SubState3,
  selector4: suspend (State) -> SubState4,
  selector5: suspend (State) -> SubState5,
  selector6: suspend (State) -> SubState6,
  selector7: suspend (State) -> SubState7,
  selector8: suspend (State) -> SubState8,
  selector9: suspend (State) -> SubState9,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4, subState5: SubState5, subState6: SubState6, subState7: SubState7, subState8: SubState8, subState9: SubState9) -> Result
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7,
    selector8,
    selector9
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7,
      it[7] as SubState8,
      it[8] as SubState9
    )
  }
)

// -------------------------------------------------------------------------------------------------

public fun <State, Result> StateFlow<State>.selectAsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector: (State) -> Result
): StateFlow<Result> = map(selector).stateIn(
  scope = scope,
  started = started,
  initialValue = selector(value)
)

public fun <State, SubState1, SubState2, Result> StateFlow<State>.select2AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  projector: (subState1: SubState1, subState2: SubState2) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(selector1, selector2),
  projector = { projector(it[0] as SubState1, it[1] as SubState2) }
)

public fun <State, SubState1, SubState2, SubState3, Result> StateFlow<State>.select3AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  projector: (subState1: SubState1, subState2: SubState2, subState3: SubState3) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(selector1, selector2, selector3),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, Result> StateFlow<State>.select4AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  projector: (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(selector1, selector2, selector3, selector4),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, Result> StateFlow<State>.select5AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  selector5: (State) -> SubState5,
  projector: (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5
  ) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, Result> StateFlow<State>.select6AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  selector5: (State) -> SubState5,
  selector6: (State) -> SubState6,
  projector: (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5,
    subState6: SubState6
  ) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, Result> StateFlow<State>.select7AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  selector5: (State) -> SubState5,
  selector6: (State) -> SubState6,
  selector7: (State) -> SubState7,
  projector: (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5,
    subState6: SubState6,
    subState7: SubState7
  ) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, SubState8, Result> StateFlow<State>.select8AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  selector5: (State) -> SubState5,
  selector6: (State) -> SubState6,
  selector7: (State) -> SubState7,
  selector8: (State) -> SubState8,
  projector: (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5,
    subState6: SubState6,
    subState7: SubState7,
    subState8: SubState8
  ) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7,
    selector8
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7,
      it[7] as SubState8
    )
  }
)

public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, SubState6, SubState7, SubState8, SubState9, Result> StateFlow<State>.select9AsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted,
  selector1: (State) -> SubState1,
  selector2: (State) -> SubState2,
  selector3: (State) -> SubState3,
  selector4: (State) -> SubState4,
  selector5: (State) -> SubState5,
  selector6: (State) -> SubState6,
  selector7: (State) -> SubState7,
  selector8: (State) -> SubState8,
  selector9: (State) -> SubState9,
  projector: (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5,
    subState6: SubState6,
    subState7: SubState7,
    subState8: SubState8,
    subState9: SubState9
  ) -> Result
): StateFlow<Result> = selectStateInternal(
  scope = scope,
  started = started,
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
    selector6,
    selector7,
    selector8,
    selector9
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
      it[5] as SubState6,
      it[6] as SubState7,
      it[7] as SubState8,
      it[8] as SubState9
    )
  }
)
