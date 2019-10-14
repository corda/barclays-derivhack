package net.corda.cdmsupport.transactionbuilding

import net.corda.cdmsupport.CDMEvent
import net.corda.cdmsupport.ExecutionAlreadyExists
import net.corda.cdmsupport.eventparsing.serializeCdmObjectIntoJson
import net.corda.cdmsupport.extensions.*
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.states.TransferState
import net.corda.cdmsupport.vaultquerying.CdmVaultQuery
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import org.isda.cdm.*


class CdmTransactionBuilder(notary: Party? = null,
                            val event: Event,
                            val cdmVaultQuery: CdmVaultQuery) : TransactionBuilder(notary) {

    val participantsFromInputs = mutableSetOf<AbstractParty>()

    init {

        event.primitive.allocation?.forEach { processAllocationPrimitive(it) }
        event.primitive.transfer?.forEach { processTransferPrimitive(it) }
        event.primitive.execution?.forEach { processeExecutionPrimitive(it) }

    }

    @Throws(RuntimeException::class)
    private fun processAllocationPrimitive(allocationPrimitive: AllocationPrimitive) {

        val executionLineage = event.lineage.executionReference[0].globalReference

        if (allocationPrimitive.validateLineageAndTotals(serviceHub!!, executionLineage)) {
            val inputState = cdmVaultQuery.getCdmExecutionStateByMetaGlobalKey(executionLineage)
            addInputState(inputState)

            val outputBeforeState = createExecutionState(allocationPrimitive.after.originalTrade.execution)

            val outputAfterStates = allocationPrimitive.after.allocatedTrade.map { createExecutionStateFromAfterAllocation(it.execution) }

            val outputIndexOnBefore = this.addOutputStateReturnIndex(outputBeforeState, CDMEvent.ID)
            addCommand(CDMEvent.Commands.Execution(outputIndexOnBefore), outputBeforeState.participants.map { it.owningKey }.toSet().toList())

            outputAfterStates.forEach {
                val outputIndexOnAfter = this.addOutputStateReturnIndex(it, CDMEvent.ID)
                addCommand(CDMEvent.Commands.Execution(outputIndexOnAfter), it.participants.map { p -> p.owningKey }.toSet().toList())
            }
        }
    }

    private fun processTransferPrimitive(transferPrimitive: TransferPrimitive) {
        val outputTransferState = createTransferState(transferPrimitive)
        val outputTransferIndex = addOutputStateReturnIndex(outputTransferState, CDMEvent.ID)

        addCommand(CDMEvent.Commands.Transfer(outputTransferIndex), this.outputStates().flatMap { it.data.participants }.map { it.owningKey }.toSet().toList())
    }

    private fun processeExecutionPrimitive(executionPrimitive: ExecutionPrimitive) {
        if  (cdmVaultQuery.getExecutions().any { it.meta.globalKey == executionPrimitive.after.execution.meta.globalKey }) {
            throw ExecutionAlreadyExists(executionPrimitive.after.execution.meta.globalKey)
        }

        val outputState = createExecutionState(executionPrimitive.after.execution)
        val outputIndex = addOutputStateReturnIndex(outputState, CDMEvent.ID)
        addCommand(CDMEvent.Commands.Execution(outputIndex), this.outputStates().flatMap { it.data.participants }.map { it.owningKey }.toSet().toList())
    }

    fun getPartiesToSign(): List<Party> {
        return this.commands.flatMap { it.signers }.toSet().map { serviceHub!!.networkMapCache.getNodesByLegalIdentityKey(it).first().legalIdentities.first() }
    }

    override fun addInputState(stateAndRef: StateAndRef<*>): TransactionBuilder {
        participantsFromInputs.addAll(stateAndRef.state.data.participants)
        return super.addInputState(stateAndRef)
    }

    fun addOutputStateReturnIndex(state: ContractState, contract: ContractClassName): Int {
        addOutputState(state, contract)
        return indexOfCurrentOutputState()
    }

    private fun createExecutionState(execution: Execution): ExecutionState {
        val executionWithParties : Execution = execution.createExecutionWithPartiesFromEvent(event)
        val json = serializeCdmObjectIntoJson(executionWithParties)
        val participants = executionWithParties.mapPartyToCordaX500ForExecution(serviceHub!!)

        return ExecutionState(json, event.meta.globalKey, AffirmationStatusEnum.UNAFFIRMED.name, participants, UniqueIdentifier())
    }

    private fun createExecutionStateFromAfterAllocation(execution: Execution) : ExecutionState {
        val executionWithParties : Execution = execution.createExecutionWithPartiesFromEvent(event)
        val json = serializeCdmObjectIntoJson(executionWithParties)
        val participants = executionWithParties.mapPartyToCordaX500ForAllocation(serviceHub!!)

        return ExecutionState(json, event.meta.globalKey, AffirmationStatusEnum.UNAFFIRMED.name, participants, UniqueIdentifier())
    }

    private fun createTransferState(transfer: TransferPrimitive): TransferState {
        val json = serializeCdmObjectIntoJson(transfer)
        val participants = transfer.mapPartyToCordaX500(serviceHub!!, event.lineage.executionReference[0].globalReference)

        return TransferState(json, event.lineage.eventReference[0].globalReference,
                event.lineage.executionReference[0].globalReference, participants, UniqueIdentifier())
    }

    private fun indexOfCurrentOutputState(): Int {
        return outputStates().size - 1
    }

    private fun getAllParticipantsAcrossAllInputsAndOutputs(): List<AbstractParty> {
        return (participantsFromInputs + outputStates().flatMap { it.data.participants }.toSet()).toList()
    }
}
