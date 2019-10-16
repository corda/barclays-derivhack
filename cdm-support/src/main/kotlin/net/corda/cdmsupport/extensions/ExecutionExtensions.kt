package net.corda.cdmsupport.extensions

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import org.isda.cdm.Event
import org.isda.cdm.Execution
import org.isda.cdm.PartyRoleEnum
import org.isda.cdm.metafields.ReferenceWithMetaParty

fun Execution.createExecutionWithPartiesFromEvent(event: Event) : Execution {

    var executionWithParties : Execution = this

    val parties = mutableSetOf<org.isda.cdm.Party>()
    this.partyRole.forEach { party -> parties.add(event.party.first { it.meta.globalKey == party.partyReference.globalReference })}
    parties.forEach { party -> executionWithParties = executionWithParties.toBuilder()
            .addParty(ReferenceWithMetaParty.ReferenceWithMetaPartyBuilder()
                    .setValue(party)
                    .setGlobalReference(party.meta.globalKey)
                    .build()).build() }

    return executionWithParties
}

fun Execution.mapPartyToCordaX500ForExecution(serviceHub : ServiceHub) : List<Party>{

    val parties : MutableSet<Party> = mutableSetOf()
    this.party.forEach { parties.add(serviceHub.identityService
            .wellKnownPartyFromX500Name(CordaX500Name.parse("O=${it.value.name.value},L=New York,C=US"))!!) }

    return parties.toList()
}

fun Execution.mapPartyToCordaX500ForAllocation(serviceHub : ServiceHub) : List<Party>{

    val parties : MutableSet<Party> = mutableSetOf()
    this.party.forEach { parties.add(serviceHub.identityService
            .wellKnownPartyFromX500Name(CordaX500Name.parse("O=${it.value.name.value},L=New York,C=US"))!!) }

    return parties.toList()
}


