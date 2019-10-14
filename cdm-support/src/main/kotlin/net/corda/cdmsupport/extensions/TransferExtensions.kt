package net.corda.cdmsupport.extensions

import net.corda.cdmsupport.vaultquerying.DefaultCdmVaultQuery
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import org.isda.cdm.AdjustableDate
import org.isda.cdm.AdjustableOrRelativeDate
import org.isda.cdm.SettlementTerms
import org.isda.cdm.TransferPrimitive

fun TransferPrimitive.mapPartyToCordaX500(serviceHub : ServiceHub, executionRef: String) : List<Party>{

    val partiesRefs : MutableSet<String> = mutableSetOf()
    val parties : MutableList<Party> = mutableListOf()

    this.securityTransfer.forEach {
        partiesRefs.add(it.transferorTransferee.transfereePartyReference.globalReference)
        partiesRefs.add(it.transferorTransferee.transferorPartyReference.globalReference)
    }
    this.cashTransfer.forEach {
        partiesRefs.add(it.payerReceiver.payerPartyReference.globalReference)
        partiesRefs.add(it.payerReceiver.receiverPartyReference.globalReference)
    }


    val partyList = DefaultCdmVaultQuery(serviceHub).getExecutions()
            .firstOrNull { it.meta.globalKey  == executionRef }?.party?.filter { p -> partiesRefs.contains(p.globalReference) }

    partyList?.forEach { parties.add(serviceHub.identityService
            .wellKnownPartyFromX500Name(net.corda.core.identity.CordaX500Name.parse("O=${it.value.name.value},L=New York,C=US"))!!) }

    return parties
}



