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
class AllocationFlow(val allocationJson: String) : FlowLogic<Unit>() {

    //TODO
    /**
     *  You're expected to work towards the JSON files for the allocation event provided for the
     *  Use Case 2 (UC2_Allocation_Trade_AT1.json ...), by using the parseEventFromJson function
     *  from the cdm-support package and ingest/consume the allocation trades on Corda,
     *  demonstrate lineage to the block trade from UC1 and validate the trade
     *  against CDM data rules by creating validations similar to those for UC1.
     *
     *  Bonus: Instead of just loading the ready to use Json fil, you can build your own
     *  Allocation Event, using the Allocate functions of the CDM and the Corda Implementation
     *  and builders that you can find in net.corda.cdmsupport.builders package in the project
     *
     *  Add an Observery mode to the transaction
     */

    @Suspendable
    override fun call() {

    }
}
