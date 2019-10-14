package com.derivhack

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class ObserveryFlow(private val regulator: Party, private val finalTx: SignedTransaction) : FlowLogic<Unit>() {

    //TODO
    /**
     * You're expected to create an Observery for each transaction
     */

    @Suspendable
    override fun call() {

    }
}

