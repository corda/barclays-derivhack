package net.corda.cdmsupport.events

import org.junit.Test

class ExecutionTest : BaseEventTest() {

    @Test
    fun execution() {
        sendNewTradeInAndCheckAssertions("UC1_block_execute_BT1.json")
    }
}
