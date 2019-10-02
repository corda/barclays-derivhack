package net.corda.cdmsupport.eventparsing

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import net.corda.cdmsupport.transactionbuilding.CdmTransactionBuilder
import org.isda.cdm.*

fun readEventFromJson(pathToResource: String): Event {
    val json = readTextFromFile(pathToResource)
    return parseEventFromJson(json)
}

fun parseEventFromJson(json: String): Event {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.readValue<Event>(json, Event::class.java)
}

fun readTextFromFile(pathToResource: String): String {
    return CdmTransactionBuilder::class.java.getResource(pathToResource).readText()
}

