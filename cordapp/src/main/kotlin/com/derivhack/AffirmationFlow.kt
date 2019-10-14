package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class AffirmationFlow(val executionRef: String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to generate relevant CDM objects and link them to associated allocated
     *  trades created with UC2 as well as validate them against CDM data rules by
     *  creating validations similar to those for the previous use cases
     *
     *  For building your affirmation CDM Object see net.corda.cdmsupport.builders
     *  package in the project
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}

