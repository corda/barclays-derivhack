package net.corda.cdmsupport.extensions

import net.corda.cdmsupport.AllocatedTotalsNotMatch
import net.corda.cdmsupport.AllocationLineageNotMatch
import net.corda.cdmsupport.states.ExecutionState
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import org.isda.cdm.AllocationPrimitive
import java.math.BigDecimal

fun AllocationPrimitive.validateLineageAndTotals(serviceHub: ServiceHub, lineageExecutionRef: String) : Boolean {

    //Validate lineage
    val globalKeyOnBefore = this.before.execution.meta.globalKey
    val globalKeyOnOriginalTrade = this.after.originalTrade.execution.meta.globalKey

    val allExecutions = serviceHub.vaultService.queryBy<ExecutionState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), PageSpecification(DEFAULT_PAGE_NUM, 1000)).states
    val matchingExecutions = allExecutions.filter { it.state.data.execution().meta.globalKey == lineageExecutionRef }

    //Ensure sum of allocation is equal to block trade
    var allocatedTotals = BigDecimal.ZERO
    this.after.allocatedTrade.forEach { allocatedTotals = allocatedTotals.plus(it.execution.quantity.amount)  }

    if ((!matchingExecutions.isNullOrEmpty() && lineageExecutionRef == globalKeyOnBefore) && globalKeyOnOriginalTrade == globalKeyOnBefore){

        val matchingExecutionsQuantity = matchingExecutions.first().state.data.execution().quantity.amount

        if (this.before.execution.quantity.amount == allocatedTotals && matchingExecutionsQuantity == allocatedTotals) {
            return true
        }

        throw AllocatedTotalsNotMatch(allocatedQuantity = allocatedTotals.toString(),
                beforeQuantity = this.before.execution.quantity.amount.toString())
    }

    throw AllocationLineageNotMatch(lineageExecutionRef, this.before.execution.meta.globalKey)
}