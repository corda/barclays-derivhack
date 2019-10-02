package net.corda.cdmsupport.events

import net.corda.cdmsupport.eventparsing.readEventFromJson
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.testflow.TestFlowInitiating
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.internal.startFlow
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AllocationTests : BaseEventTest() {

    @Test
    fun allocation() {

        // --------- new trade
        sendNewTradeInAndCheckAssertions("UC1_block_execute_BT1.json")

        //----------------allocation
        val allocationEvent = readEventFromJson("/${samplesDirectory}/UC2_allocation_execution_AT1.json")
        val future = node2.services.startFlow(TestFlowInitiating(allocationEvent)).resultFuture
        val tx = future.getOrThrow().toLedgerTransaction(node2.services)
        checkTheBasicFabricOfTheTransaction(tx, 1, 3, 0, 3)

        //look closer at the states
        val executionInputState = tx.inputStates.find { it is ExecutionState } as ExecutionState
        val cdmExecutionInputState = executionInputState.execution()
        assertNotNull(cdmExecutionInputState)
        checkIdentiferIsOnTrade(cdmExecutionInputState, "W3S0XZGEM4S82", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")

        val executionStateOutputStateOne = tx.outputStates.find { it is ExecutionState } as ExecutionState
        val executionStateOutputStateTwo = tx.outputStates.findLast { it is ExecutionState } as ExecutionState

        assertNotNull(executionStateOutputStateOne)
        assertNotNull(executionStateOutputStateTwo)

        val cdmExecutionStateOutputStateOne = executionStateOutputStateOne.execution()
        val cdmExecutionStateOutputStateTwo = executionStateOutputStateTwo.execution()

        assertNotNull(cdmExecutionStateOutputStateOne)
        assertNotNull(cdmExecutionStateOutputStateTwo)
        checkIdentiferIsOnTrade(cdmExecutionStateOutputStateOne, "W3S0XZGEM4S82", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")
        checkIdentiferIsOnTrade(cdmExecutionStateOutputStateTwo, "ST2K6U8RHX7MZ", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")

        //look closer at the commands
        assertEquals(listOf(party2.owningKey, party3.owningKey), tx.commands.get(0).signers)
        assertEquals(listOf(party1.owningKey, party2.owningKey), tx.commands.get(1).signers)
        assertEquals(listOf(party1.owningKey, party2.owningKey), tx.commands.get(2).signers)

    }


}



