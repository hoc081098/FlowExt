package com.hoc081098.flowext.internal

import kotlinx.coroutines.flow.FlowCollector

/**
 * This exception is thrown when operator need no more elements from the flow.
 * This exception should never escape outside of operator's implementation.
 * This exception can be safely ignored by non-terminal flow operator if and only if it was caught by its owner
 * (see usages of [checkOwnership]).
 */
internal class ClosedException(val owner: FlowCollector<*>) :
  Exception("Flow was aborted, no more elements needed")

internal fun ClosedException.checkOwnership(owner: FlowCollector<*>) {
  if (this.owner !== owner) throw this
}
