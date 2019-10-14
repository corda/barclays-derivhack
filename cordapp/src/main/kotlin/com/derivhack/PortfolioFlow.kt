package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Guice
import net.corda.cdmsupport.CDMEvent
import net.corda.cdmsupport.eventparsing.*
import implementations.EvaluatePortfolioStateCordaImpl
import net.corda.cdmsupport.functions.portfolioBuilderFromExecutions
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.states.PortfolioState
import net.corda.cdmsupport.states.TransferState
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.isda.cdm.CdmRuntimeModule

@InitiatingFlow
@StartableByRPC
class PortfolioFlow(val transferRefs: List<String>,
                    val executionRefs : List<String>,
                    val pathToInstructions : String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to assemble the settled and traded positions separately and then
     *  construct a portfolio report object that includes both the positions using the
     *  outputs from UC2 and UC5 as well as validate them against CDM data rules by
     *  creating validations similar to those for the previous use cases
     *
     *  For building your portfolio see net.corda.cdmsupport.builders
     *  package in the project
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}
