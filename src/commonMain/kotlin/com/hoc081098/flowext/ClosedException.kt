package com.hoc081098.flowext

import kotlinx.coroutines.flow.FlowCollector

internal class ClosedException(val owner: FlowCollector<*>) :
  Exception("Flow was aborted, no more elements needed")
