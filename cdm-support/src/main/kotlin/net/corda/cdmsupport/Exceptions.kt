package net.corda.cdmsupport

class ValidationUnsuccessfull(message : String) : RuntimeException("Validation failed : $message")

class ExecutionAlreadyExists(ref: String) : java.lang.RuntimeException("Execution with ref $ref already exists")
class AllocatedTotalsNotMatch(beforeQuantity: String, allocatedQuantity: String) : RuntimeException("Allocated quantity not match - originalTrade : $beforeQuantity, allocated: $allocatedQuantity")
class AllocationLineageNotMatch(eventLineage: String, executionGlobalKey: String) : RuntimeException("Difference found between event linage : $eventLineage and execution reference $executionGlobalKey")

class CdmExecutionForMetaGlobalKeyNotFound(globalKey : String) : RuntimeException("Cdm execution not found in vault for global key: $globalKey")
class MultipleCdmExecutionsForMetaGlobalKeyFound(globalKey: String) : RuntimeException("Multiple event metadata found for rosetta key $globalKey")
