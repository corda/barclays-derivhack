package net.corda.cdmsupport.events

import net.corda.cdmsupport.CDMEvent
import net.corda.cdmsupport.eventparsing.readEventFromJson
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.testflow.TestFlowInitiating
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.TestStartedNode
import net.corda.testing.node.internal.cordappsForPackages
import net.corda.testing.node.internal.startFlow
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class BaseEventTest(val samplesDirectory: String = "jsons") {
    lateinit var mockNetwork: InternalMockNetwork
    lateinit var node1: TestStartedNode
    lateinit var node2: TestStartedNode
    lateinit var node3: TestStartedNode
    lateinit var node4: TestStartedNode
    lateinit var party1: Party
    lateinit var party2: Party
    lateinit var party3: Party
    lateinit var party4: Party

    @Before
    fun setup() {
        mockNetwork = InternalMockNetwork(cordappsForAllNodes = cordappsForPackages("net.corda.cdmsupport.testflow", "net.corda.cdmsupport"),
                threadPerNode = true, initialNetworkParameters = testNetworkParameters(minimumPlatformVersion = 4))
        node1 = mockNetwork.createPartyNode(CordaX500Name(organisation = "Client1", locality = "New York", country = "US"))
        node2 = mockNetwork.createPartyNode(CordaX500Name(organisation = "Broker1", locality = "New York", country = "US"))
        node3 = mockNetwork.createPartyNode(CordaX500Name(organisation = "Broker2", locality = "New York", country = "US"))
        node4 = mockNetwork.createPartyNode(CordaX500Name(organisation = "Observery", locality = "New York", country = "US"))
        party1 = node1.services.myInfo.legalIdentities.first()
        party2 = node2.services.myInfo.legalIdentities.first()
        party3 = node3.services.myInfo.legalIdentities.first()
        party4 = node4.services.myInfo.legalIdentities.first()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }


    protected fun sendNewTradeInAndCheckAssertions(jsonFileName: String) {

        val newTradeEvent = readEventFromJson("/${samplesDirectory}/$jsonFileName")
        val future = node2.services.startFlow(TestFlowInitiating(newTradeEvent)).resultFuture
        val tx = future.getOrThrow().toLedgerTransaction(node2.services)

        checkTheBasicFabricOfTheTransaction(tx, 0, 1, 0, 1)

        //look closer at the states
        val executionState = tx.outputStates.find { it is ExecutionState } as ExecutionState
        assertNotNull(executionState)
        assertEquals(listOf(party1, party2, party3), executionState.participants)

        //look closer at the commands
        assertTrue(tx.commands.get(0).value is CDMEvent.Commands.Execution)
        assertEquals(listOf(party1.owningKey, party2.owningKey, party3.owningKey), tx.commands.get(0).signers)
    }

    protected fun sendAllocationAndCheckAssertions(jsonFileName: String) {

        //----------------allocation
        val allocationEvent = readEventFromJson("/${samplesDirectory}/$jsonFileName")
        val future = node2.services.startFlow(TestFlowInitiating(allocationEvent)).resultFuture
        val tx = future.getOrThrow().toLedgerTransaction(node2.services)
        checkTheBasicFabricOfTheTransaction(tx, 1, 3, 0, 3)

        //look closer at the states
        //val executionInputState = tx.inputStates.find { it is ExecutionState } as ExecutionState
        //val cdmExecutionInputState = executionInputState.execution()
        //assertNotNull(cdmExecutionInputState)
//        checkIdentiferIsOnTrade(cdmExecutionInputState, "W3S0XZGEM4S82", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")

        val executionStateOutputStateOne = tx.outputStates[0] as ExecutionState
        val executionStateOutputStateTwo = tx.outputStates[1] as ExecutionState
        val executionStateOutputStateThree = tx.outputStates[2] as ExecutionState


        assertNotNull(executionStateOutputStateOne)
        assertNotNull(executionStateOutputStateTwo)
        assertNotNull(executionStateOutputStateThree)

        val cdmExecutionStateOutputStateOne = executionStateOutputStateOne.execution()
        val cdmExecutionStateOutputStateTwo = executionStateOutputStateTwo.execution()
        val cdmExecutionStateOutputStateThree = executionStateOutputStateThree.execution()


        assertNotNull(cdmExecutionStateOutputStateOne)
        assertNotNull(cdmExecutionStateOutputStateTwo)
        assertNotNull(cdmExecutionStateOutputStateThree)
//        checkIdentiferIsOnTrade(cdmExecutionStateOutputStateOne, "W3S0XZGEM4S82", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")
//        checkIdentiferIsOnTrade(cdmExecutionStateOutputStateTwo, "ST2K6U8RHX7MZ", "3vqQOOnXah+v+Cwkdh/hSyDP7iD6lLGqRDW/500GvjU=")

        //look closer at the commands
        assertEquals(listOf(party1.owningKey, party2.owningKey, party3.owningKey), tx.commands.get(0).signers)
        assertEquals(listOf(party1.owningKey, party2.owningKey, party3.owningKey), tx.commands.get(1).signers)
        assertEquals(listOf(party1.owningKey, party2.owningKey, party3.owningKey), tx.commands.get(2).signers)
    }

    //confirming things
    protected fun checkTheBasicFabricOfTheTransaction(tx: LedgerTransaction, numInputStates: Int, numOutputStates: Int, numReferenceStates: Int, numCommands: Int) {
        assertEquals(numInputStates, tx.inputs.size)
        assertEquals(numReferenceStates, tx.referenceStates.size)
        assertEquals(numOutputStates, tx.outputStates.size)
        assertEquals(numCommands, tx.commands.size)
    }
}



