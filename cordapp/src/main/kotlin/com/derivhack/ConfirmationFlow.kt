package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.cdmsupport.CDMEvent
import net.corda.cdmsupport.ValidationUnsuccessfull
import net.corda.cdmsupport.eventparsing.serializeCdmObjectIntoJson
import net.corda.cdmsupport.functions.confirmationBuilderFromExecution
import net.corda.cdmsupport.states.ConfirmationState
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.validators.CdmValidators
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.isda.cdm.ConfirmationStatusEnum

@InitiatingFlow
@StartableByRPC
class ConfirmationFlow(val executionRef: String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to generate relevant CDM objects and link them to associated allocated
     *  trades created with UC3 as well as validate them against CDM data rules by
     *  creating validations similar to those for the previous use cases
     *
     *  For building your confirmation CDM Object see net.corda.cdmsupport.builders
     *  package in the project
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}
