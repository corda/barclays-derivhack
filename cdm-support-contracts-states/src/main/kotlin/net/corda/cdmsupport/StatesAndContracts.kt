package net.corda.cdmsupport

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
import org.isda.cdm.Event
import java.math.BigDecimal
import java.time.LocalDate

class CDMEvent : Contract {


    companion object {
        val ID = "net.corda.cdmsupport.CDMEvent"
    }

    interface Commands : CommandData {
        class Affirmation() : Commands
        class Confirmation() : Commands
        class Portfolio() : Commands
        class Execution(val outputIndex: Int) : Commands
        class Transfer(val outputIndex: Int) : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        // TODO: Write the verify logic.
    }


}











