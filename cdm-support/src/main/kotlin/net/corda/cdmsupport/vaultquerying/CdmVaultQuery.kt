package net.corda.cdmsupport.vaultquerying

import net.corda.cdmsupport.CdmExecutionForMetaGlobalKeyNotFound
import net.corda.cdmsupport.MultipleCdmExecutionsForMetaGlobalKeyFound
import net.corda.cdmsupport.states.AffirmationState
import net.corda.cdmsupport.states.ConfirmationState
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.states.TransferState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import org.isda.cdm.*


interface CdmVaultQuery {

    //Execution State Query
    fun getCdmExecutionStateByMetaGlobalKey(globalKey: String): StateAndRef<ExecutionState>

    fun getExecutions() : List<Execution>
    fun getAffirmedExecutions() : List<Execution>
    fun getConfirmedExecutions() : List<Execution>
    fun getSettlements() : List<TransferPrimitive>
    fun getSettledSettlements(): List<TransferPrimitive>

    fun getAffirmations(): List<Affirmation>
    fun getConfirmations(): List<Confirmation>
}

    class DefaultCdmVaultQuery(val serviceHub: ServiceHub, val pageSize : Int = 1000) : CdmVaultQuery {

        override fun getExecutions(): List<Execution> {
            return serviceHub.vaultService.queryBy<ExecutionState>().states.map { it.state.data.execution() }
        }

        override fun getAffirmedExecutions(): List<Execution> {
            return serviceHub.vaultService
                    .queryBy<ExecutionState>().states
                    .filter { it.state.data.workflowStatus == AffirmationStatusEnum.AFFIRMED.name }
                    .map { ex -> ex.state.data.execution() }
        }

        override fun getConfirmedExecutions(): List<Execution> {

            return serviceHub.vaultService
                    .queryBy<ExecutionState>().states
                    .filter { it.state.data.workflowStatus == ConfirmationStatusEnum.CONFIRMED.name }
                    .map { ex -> ex.state.data.execution() }
        }

        override fun getAffirmations(): List<Affirmation> {
            return serviceHub.vaultService.queryBy<AffirmationState>().states.map { it.state.data.affirmation() }
        }

        override fun getConfirmations(): List<Confirmation> {
            return serviceHub.vaultService.queryBy<ConfirmationState>().states.map { it.state.data.confirmation() }
        }

        override fun getSettlements(): List<TransferPrimitive> {
            return serviceHub.vaultService.queryBy<TransferState>().states.map { it.state.data.transfer() }
        }

        override fun getSettledSettlements(): List<TransferPrimitive> {
            return serviceHub.vaultService.queryBy<TransferState>().states
                    .filter { it.state.data.transfer().status == TransferStatusEnum.SETTLED }
                    .map { it.state.data.transfer() }
        }

        override fun getCdmExecutionStateByMetaGlobalKey(globalKey: String): StateAndRef<ExecutionState> {
            val allContracts = serviceHub.vaultService.queryBy<ExecutionState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), PageSpecification(DEFAULT_PAGE_NUM, pageSize)).states

            //look at all the executions, all their party contract identifiers and see if any of them match the execution references we are looking for
            val matchingContracts = allContracts.filter { it.state.data.execution().meta.globalKey == globalKey }
            when {
                matchingContracts.isEmpty() -> throw CdmExecutionForMetaGlobalKeyNotFound(globalKey)
                matchingContracts.size > 1 -> throw MultipleCdmExecutionsForMetaGlobalKeyFound(globalKey)
                else -> return matchingContracts.get(0)
            }
        }
    }
