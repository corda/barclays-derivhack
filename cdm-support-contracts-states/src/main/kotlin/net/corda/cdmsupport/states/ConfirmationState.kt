package net.corda.cdmsupport.states

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import net.corda.cdmsupport.CDMEvent
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(CDMEvent::class)
class ConfirmationState(val confirmationJson: String,
                        override val participants: List<Party>,
                        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    fun confirmation(): org.isda.cdm.Confirmation {
        val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
        return rosettaObjectMapper.readValue<org.isda.cdm.Confirmation>(confirmationJson, org.isda.cdm.Confirmation::class.java)
    }
}

