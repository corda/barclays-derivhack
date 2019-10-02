package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class AllocationFlow(val allocationJson: String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to work towards the JSON file for the allocation event provided for the
     *  Use Case 2 (UC2_allocation_execution_AT1.json), by using the parseEventFromJson function
     *  from the cdm-support package and ingest/consume the allocation trades on Corda,
     *  demonstrate lineage to the block trade from Use Case 1 and validate the trade
     *  against CDM data rules by creating validations similar to those for Use Case 1.
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}
