package net.corda.cdmsupport.events

import net.corda.cdmsupport.CDMEvent
import net.corda.cdmsupport.eventparsing.readEventFromJson
import net.corda.cdmsupport.states.AffirmationState
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.testflow.TestAffirmationFlow
import net.corda.cdmsupport.testflow.TestFlowInitiating
import net.corda.cdmsupport.validators.CdmValidators
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.internal.startFlow
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AffirmationTests : BaseEventTest() {

    @Test
    fun affirmation() {
        sendNewTradeInAndCheckAssertions("UC1_block_execute_BT1.json")

        //----------------allocation
        val allocationEvent = readEventFromJson("/${samplesDirectory}/UC2_allocation_execution_AT1.json")
        val allocationFuture = node2.services.startFlow(TestFlowInitiating(allocationEvent)).resultFuture
        val allocationTx = allocationFuture.getOrThrow().toLedgerTransaction(node2.services)
        checkTheBasicFabricOfTheTransaction(allocationTx, 1, 3, 0, 3)

        val future = node1.services.startFlow(TestAffirmationFlow("vkFNMnTu1Fnk/p1gktgvz040El1XFnMwxYAVdILDlto=")).resultFuture

        val tx = future.getOrThrow().toLedgerTransaction(node1.services)

        checkTheBasicFabricOfTheTransaction(tx, 1, 2, 0, 1)

        val inputState = tx.inputStates.find { it is ExecutionState } as ExecutionState

        val affirmationState = tx.outputStates.find { it is AffirmationState } as AffirmationState
        val executionState = tx.outputStates.find { it is ExecutionState } as ExecutionState

        CdmValidators().validateAffirmation(affirmationState.affirmation())

        assertNotNull(inputState)
        assertNotNull(affirmationState)
        assertNotNull(executionState)

        //look closer at the commands
        assertTrue(tx.commands.get(0).value is CDMEvent.Commands.Affirmation)
        assertEquals(listOf(party1.owningKey, party2.owningKey), tx.commands.get(0).signers)
    }

}
