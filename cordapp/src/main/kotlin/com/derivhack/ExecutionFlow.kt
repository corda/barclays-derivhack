package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.cdmsupport.ValidationUnsuccessfull
import net.corda.cdmsupport.eventparsing.parseEventFromJson
import net.corda.cdmsupport.transactionbuilding.CdmTransactionBuilder
import net.corda.cdmsupport.validators.CdmValidators
import net.corda.cdmsupport.vaultquerying.DefaultCdmVaultQuery
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class ExecutionFlow(val executionJson: String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to convert trades from CDM representation to work towards Corda by loading
     *  the JSON files for the execution events provided for the UC1 (UC1_Block_Trade_BT1.json ...),
     *  and using the parseEventFromJson function from the cdm-support package to
     *  create an Execution CDM Object and Execution State working with the CDMTransactionBuilder as well
     *  as also validate the trade against CDM data rules by using the CDMValidators.
     *
     *  Add an Observery mode to the transaction
     */


    @Suspendable
    override fun call() {

    }
}
