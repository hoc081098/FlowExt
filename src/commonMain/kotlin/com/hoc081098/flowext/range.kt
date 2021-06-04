package com.hoc081098.flowext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun range(start: Int, count: Int): Flow<Int> = flow {
    repeat(count) { emit(it + start) }
}
