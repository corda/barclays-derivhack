package net.corda.cdmsupport.states

import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import net.corda.cdmsupport.CDMEvent
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(CDMEvent::class)
data class AffirmationState(
        val affirmationJson: String,
        override val participants: List<Party>,
        override val linearId:  UniqueIdentifier = UniqueIdentifier()) : LinearState {

    fun affirmation(): org.isda.cdm.Affirmation {
        val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
        return rosettaObjectMapper.readValue<org.isda.cdm.Affirmation>(affirmationJson, org.isda.cdm.Affirmation::class.java)
    }

}
