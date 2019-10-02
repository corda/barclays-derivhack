package net.corda.cdmsupport.eventparsing

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper

fun serializeCdmObjectIntoJson(cdmObject: Any): String {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cdmObject)
}


