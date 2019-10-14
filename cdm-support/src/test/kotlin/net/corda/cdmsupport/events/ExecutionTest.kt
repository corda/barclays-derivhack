package net.corda.cdmsupport.events

import org.junit.Test

class ExecutionTest : BaseEventTest() {

    @Test
    fun execution() {
        sendNewTradeInAndCheckAssertions("UC1_Block_Trade_BT1.json")
    }
}
