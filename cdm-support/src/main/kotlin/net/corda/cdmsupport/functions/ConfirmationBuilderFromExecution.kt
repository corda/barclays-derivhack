package net.corda.cdmsupport.functions

import net.corda.cdmsupport.states.ExecutionState
import org.isda.cdm.Confirmation

fun confirmationBuilderFromExecution(state : ExecutionState) : Confirmation {

    //TODO Add your code for building CDM Confirmation Object here*

    return Confirmation.ConfirmationBuilder().build()
}
