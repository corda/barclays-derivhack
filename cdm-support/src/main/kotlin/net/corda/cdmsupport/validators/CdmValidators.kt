package net.corda.cdmsupport.validators

import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.Validator
import org.isda.cdm.*
import org.isda.cdm.meta.*

class CdmValidators() {

    fun validateEvent(event: Event): List<ValidationResult<in Event>> {
        val eventMeta = EventMeta()
        val validators = ArrayList<Validator<in Event>>()
        validators.addAll(eventMeta.choiceRuleValidators())
        validators.addAll(eventMeta.dataRules())
        validators.add(eventMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("Event"), event) }.toList()
    }

    fun validateExecution(execution: Execution){
        //TODO Your code here
    }

    fun validateExecutionPrimitive(executionPrimitive: ExecutionPrimitive) {
        //TODO Your code here
    }

    fun validateAllocationPrimitive(allocationPrimitive: AllocationPrimitive) {
        //TODO Your code here
    }

    fun validateAffirmation(affirmation: Affirmation) {
        //TODO Your code here
    }

}
