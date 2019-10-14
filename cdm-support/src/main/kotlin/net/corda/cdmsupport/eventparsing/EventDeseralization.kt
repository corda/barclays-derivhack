package net.corda.cdmsupport.eventparsing

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import com.rosetta.model.lib.records.Date
import net.corda.cdmsupport.transactionbuilding.CdmTransactionBuilder
import org.isda.cdm.*
import org.isda.cdm.metafields.ReferenceWithMetaParty

fun readEventFromJson(pathToResource: String): Event {
    val json = readTextFromFile(pathToResource)
    return parseEventFromJson(json)
}

fun parseEventFromJson(json: String): Event {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.readValue<Event>(json, Event::class.java)
}

fun parsePartyFromJson(json: String) : Party {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.readValue<Party>(json, Party::class.java)
}

fun parseProductFromJson(json: String) : Product {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.readValue<Product>(json, Product::class.java)
}

fun parseDateFromJson(json : String) : Date {
    val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
    return rosettaObjectMapper.readValue<Date>(json, Date::class.java)
}

fun readTextFromFile(pathToResource: String): String {
    return CdmTransactionBuilder::class.java.getResource(pathToResource).readText()
}

