package net.corda.cdmsupport.validators

import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.Validator
import org.isda.cdm.*
import org.isda.cdm.meta.*


class CdmValidators() {

    fun validateExecution(execution: Execution): List<ValidationResult<in Execution>> {
        val executionMeta = ExecutionMeta()
        val validators = ArrayList<Validator<in Execution>>()
        validators.addAll(executionMeta.choiceRuleValidators())
        validators.addAll(executionMeta.dataRules())
        validators.add(executionMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("Execution"), execution) }.toList()
    }

    fun validateEvent(event: Event): List<ValidationResult<in Event>> {
        val eventMeta = EventMeta()
        val validators = ArrayList<Validator<in Event>>()
        validators.addAll(eventMeta.choiceRuleValidators())
        validators.addAll(eventMeta.dataRules())
        validators.add(eventMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("Event"), event) }.toList()
    }

    fun validateExecutionPrimitive(executionPrimitive: ExecutionPrimitive): List<ValidationResult<in ExecutionPrimitive>> {
        val executionPrimitiveMeta = ExecutionPrimitiveMeta()
        val validators = ArrayList<Validator<in ExecutionPrimitive>>()
        validators.addAll(executionPrimitiveMeta.choiceRuleValidators())
        validators.addAll(executionPrimitiveMeta.dataRules())
        validators.add(executionPrimitiveMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("ExecutionPrimitive"), executionPrimitive) }.toList()
    }

    fun validateAllocationPrimitive(allocationPrimitive: AllocationPrimitive): List<ValidationResult<in AllocationPrimitive>> {
        val allocationPrimitiveMeta = AllocationPrimitiveMeta()
        val validators = ArrayList<Validator<in AllocationPrimitive>>()
        validators.addAll(allocationPrimitiveMeta.choiceRuleValidators())
        validators.addAll(allocationPrimitiveMeta.dataRules())
        validators.add(allocationPrimitiveMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("AllocationPrimitive"), allocationPrimitive) }.toList()
    }

    fun validateTransferPrimitive(transferPrimitive: TransferPrimitive): List<ValidationResult<in TransferPrimitive>> {
        val transferPrimitiveMeta = TransferPrimitiveMeta()
        val validators = ArrayList<Validator<in TransferPrimitive>>()
        validators.addAll(transferPrimitiveMeta.choiceRuleValidators())
        validators.addAll(transferPrimitiveMeta.dataRules())
        validators.add(transferPrimitiveMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("TransferPrimitive"), transferPrimitive) }.toList()
    }

    fun validateAffirmation(affirmation: Affirmation): List<ValidationResult<in Affirmation>> {
        val affirmationMeta = AffirmationMeta()
        val validators = ArrayList<Validator<in Affirmation>>()
        validators.addAll(affirmationMeta.choiceRuleValidators())
        validators.addAll(affirmationMeta.dataRules())
        validators.add(affirmationMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("Affirmation"), affirmation) }.toList()
    }

    fun validateConfirmation(confirmation: Confirmation): List<ValidationResult<in Confirmation>> {
        val confirmationMeta = ConfirmationMeta()
        val validators = ArrayList<Validator<in Confirmation>>()
        validators.addAll(confirmationMeta.choiceRuleValidators())
        validators.addAll(confirmationMeta.dataRules())
        validators.add(confirmationMeta.validator())

        return validators.map { it.validate(RosettaPath.valueOf("Confirmation"), confirmation) }.toList()
    }
}
