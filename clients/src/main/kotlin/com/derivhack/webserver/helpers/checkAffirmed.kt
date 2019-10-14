package com.derivhack.webserver.helpers

import net.corda.cdmsupport.states.ExecutionState
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import org.isda.cdm.WorkflowStatusEnum


fun checkExecutionIsAffirmed(executionRef: String, proxy: CordaRPCOps) : Boolean {
    val allContractStatesAndRefs = proxy.vaultQueryBy<ExecutionState>().states
    val stateAndRef = allContractStatesAndRefs.first { it.state.data.execution().meta.globalKey == executionRef }
    val state = stateAndRef.state.data

    return state.workflowStatus == WorkflowStatusEnum.AFFIRMED.name

}
