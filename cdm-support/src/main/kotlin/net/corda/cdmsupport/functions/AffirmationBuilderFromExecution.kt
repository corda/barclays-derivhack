package net.corda.cdmsupport.functions

import CDMBuilders
import org.isda.cdm.Affirmation

fun affirmationBuilderFromExecution() : Affirmation {

    return CDMBuilders().affirmation

}
