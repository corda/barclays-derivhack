package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.cdmsupport.eventparsing.parseEventFromJson
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.transactionbuilding.CdmTransactionBuilder
import net.corda.cdmsupport.vaultquerying.DefaultCdmVaultQuery
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.isda.cdm.AdjustableDate
import org.isda.cdm.AdjustableOrRelativeDate
import org.isda.cdm.SettlementTerms

@InitiatingFlow
@StartableByRPC
class TransferFlow(val jsonEvent: String) : FlowLogic<Unit>() {


    //TODO
    /**
     *  You're expected to simulate transfer/settlement process, ensuring that the
     *  cash and securities transfers refer to relevant accounts through use of SSIs
     *  from the output allocated trades from UC2 as well as validate them against
     *  CDM data rules by creating validations similar to those for the previous use cases
     *
     *  For building your transfer/settlement see net.corda.cdmsupport.builders
     *  package in the project
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}
