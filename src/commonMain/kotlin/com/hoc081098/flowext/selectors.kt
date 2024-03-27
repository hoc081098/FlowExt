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

@file:Suppress("UNCHECKED_CAST", "ReplaceManualRangeWithIndicesCalls")

package com.hoc081098.flowext

import com.hoc081098.flowext.internal.INTERNAL_NULL_VALUE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

private typealias SubStateT = Any?

internal fun <State, Result> Flow<State>.select1Internal(
  selector: suspend (State) -> Result,
): Flow<Result> = flow {
  var latestState: Any? = INTERNAL_NULL_VALUE // Result | NULL_VALUE

  distinctUntilChanged().collect { state ->
    val currentState = selector(state)

    if (latestState === INTERNAL_NULL_VALUE || (latestState as Result) != currentState) {
      latestState = currentState
      emit(currentState)
    }
  }
}

private fun <State, Result> Flow<State>.selectInternal(
  selectors: Array<Selector<State, SubStateT>>,
  projector: suspend (Array<SubStateT>) -> Result,
): Flow<Result> {
  require(selectors.isNotEmpty()) { "selectors must not be empty" }

  return flow {
    var latestSubStates: Array<SubStateT>? = null
    var latestState: Any? = INTERNAL_NULL_VALUE // Result | NULL_VALUE
    var reusableSubStates: Array<SubStateT>? = null

    distinctUntilChanged().collect { state ->
      val currentSubStates = reusableSubStates
        ?: arrayOfNulls<Any?>(selectors.size).also { reusableSubStates = it }

      for (i in 0 until selectors.size) {
        currentSubStates[i] = selectors[i](state)
      }

      if (latestSubStates === null || !currentSubStates.contentEquals(latestSubStates)) {
        val currentState = projector(
          currentSubStates
            .copyOf()
            .also { latestSubStates = it },
        )

        if (latestState === INTERNAL_NULL_VALUE || (latestState as Result) != currentState) {
          latestState = currentState
          emit(currentState)
        }
      }
    }
  }
}

// -------------------------------------------------------------------------------------------------

/**
 * Inspirited by [NgRx memoized selector](https://ngrx.io/guide/store/selectors).
 *
 * Selectors are pure functions used for obtaining slices of a Flow of state.
 * `FlowExt` provides a few helper functions for optimizing this selection.
 *
 * - Selectors can compute derived data, to store the minimal possible state.
 * - Selectors are efficient. A selector is not recomputed unless one of its arguments changes.
 * - When using the [select] functions, it will keep track of the latest arguments in which your selector function was invoked.
 *   Because selectors are pure functions, the last result can be returned
 *   when the arguments match without re-invoking your selector function.
 *   This can provide performance benefits, particularly with selectors that perform expensive computation.
 *   This practice is known as memoization.
 */
public typealias Selector<State, SubState> = suspend (State) -> SubState

/**
 * Select a sub-state from the [State] and emit it if it is different from the previous one.
 *
 * @param selector A function that takes the [State] and returns a sub-state.
 */
public fun <State, Result> Flow<State>.select(selector: Selector<State, Result>): Flow<Result> =
  select1Internal(selector = selector)

/**
 * Select two sub-states from the source [Flow] and combine them into a [Result].
 *
 * The [projector] will be invoked only when one of the sub-states is changed.
 * The returned flow will emit the result of [projector],
 * and all subsequent results repetitions of the same value are filtered out
 * (same as [distinctUntilChanged]).
 *
 * @param selector1 The first selector to be used to select first sub-states.
 * @param selector2 The second selector to be used to select second sub-states.
 * @param projector The projector to be used to combine the sub-states into a result.
 */
public fun <State, SubState1, SubState2, Result> Flow<State>.select(
  selector1: Selector<State, SubState1>,
  selector2: Selector<State, SubState2>,
  projector: suspend (subState1: SubState1, subState2: SubState2) -> Result,
): Flow<Result> = selectInternal(
  selectors = arrayOf(selector1, selector2),
  projector = { projector(it[0] as SubState1, it[1] as SubState2) },
)

/**
 * Select three sub-states from the source [Flow] and combine them into a [Result].
 *
 * The [projector] will be invoked only when one of the sub-states is changed.
 * The returned flow will emit the result of [projector],
 * and all subsequent results repetitions of the same value are filtered out
 * (same as [distinctUntilChanged]).
 *
 * @param selector1 The first selector to be used to select first sub-states.
 * @param selector2 The second selector to be used to select second sub-states.
 * @param selector3 The third selector to be used to select third sub-states.
 * @param projector The projector to be used to combine the sub-states into a result.
 */
public fun <State, SubState1, SubState2, SubState3, Result> Flow<State>.select(
  selector1: Selector<State, SubState1>,
  selector2: Selector<State, SubState2>,
  selector3: Selector<State, SubState3>,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3) -> Result,
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
    )
  },
)

/**
 * Select four sub-states from the source [Flow] and combine them into a [Result].
 *
 * The [projector] will be invoked only when one of the sub-states is changed.
 * The returned flow will emit the result of [projector],
 * and all subsequent results repetitions of the same value are filtered out
 * (same as [distinctUntilChanged]).
 *
 * @param selector1 The first selector to be used to select first sub-states.
 * @param selector2 The second selector to be used to select second sub-states.
 * @param selector3 The third selector to be used to select third sub-states.
 * @param selector4 The fourth selector to be used to select fourth sub-states.
 * @param projector The projector to be used to combine the sub-states into a result.
 */
public fun <State, SubState1, SubState2, SubState3, SubState4, Result> Flow<State>.select(
  selector1: Selector<State, SubState1>,
  selector2: Selector<State, SubState2>,
  selector3: Selector<State, SubState3>,
  selector4: Selector<State, SubState4>,
  projector: suspend (subState1: SubState1, subState2: SubState2, subState3: SubState3, subState4: SubState4) -> Result,
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
    )
  },
)

/**
 * Select five sub-states from the source [Flow] and combine them into a [Result].
 *
 * The [projector] will be invoked only when one of the sub-states is changed.
 * The returned flow will emit the result of [projector],
 * and all subsequent results repetitions of the same value are filtered out
 * (same as [distinctUntilChanged]).
 *
 * @param selector1 The first selector to be used to select first sub-states.
 * @param selector2 The second selector to be used to select second sub-states.
 * @param selector3 The third selector to be used to select third sub-states.
 * @param selector4 The fourth selector to be used to select fourth sub-states.
 * @param selector5 The fifth selector to be used to select fifth sub-states.
 * @param projector The projector to be used to combine the sub-states into a result.
 */
public fun <State, SubState1, SubState2, SubState3, SubState4, SubState5, Result> Flow<State>.select(
  selector1: Selector<State, SubState1>,
  selector2: Selector<State, SubState2>,
  selector3: Selector<State, SubState3>,
  selector4: Selector<State, SubState4>,
  selector5: Selector<State, SubState5>,
  projector: suspend (
    subState1: SubState1,
    subState2: SubState2,
    subState3: SubState3,
    subState4: SubState4,
    subState5: SubState5,
  ) -> Result,
): Flow<Result> = selectInternal(
  selectors = arrayOf(
    selector1,
    selector2,
    selector3,
    selector4,
    selector5,
  ),
  projector = {
    projector(
      it[0] as SubState1,
      it[1] as SubState2,
      it[2] as SubState3,
      it[3] as SubState4,
      it[4] as SubState5,
    )
  },
)
