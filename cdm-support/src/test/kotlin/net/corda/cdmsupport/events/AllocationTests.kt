package net.corda.cdmsupport.events

import net.corda.cdmsupport.eventparsing.readEventFromJson
import net.corda.cdmsupport.states.ExecutionState
import net.corda.cdmsupport.testflow.TestFlowInitiating
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.internal.startFlow
import org.isda.cdm.Portfolio
import org.isda.cdm.functions.EvaluatePortfolioStateImpl
import org.junit.Test
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.google.inject.Guice
import com.google.inject.Injector


class AllocationTests : BaseEventTest() {

    @Test
    fun allocation() {

        // --------- new trade
        sendNewTradeInAndCheckAssertions("UC1_Block_Trade_BT1.json")
        // --------- allocation
        sendAllocationAndCheckAssertions("UC2_Allocation_Trade_AT1.json")

    }


}



