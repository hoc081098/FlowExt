package com.hoc081098.flowext

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private object NeverFlow : Flow<Nothing> by (flow { awaitCancellation() })

/**
 * Returns a [Flow] that never emits any values.
 */
public fun neverFlow(): Flow<Nothing> = NeverFlow
