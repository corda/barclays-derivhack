package net.corda.cdmsupport.functions

import net.corda.cdmsupport.states.ExecutionState
import net.corda.core.contracts.UniqueIdentifier
import org.isda.cdm.*
import org.isda.cdm.metafields.*

fun affirmationBuilderFromExecution(state : ExecutionState) : Affirmation {
    val affirmationBuilder = Affirmation.AffirmationBuilder()
            .setLineage(Lineage.LineageBuilder()
                    .addEventReference(ReferenceWithMetaEvent.ReferenceWithMetaEventBuilder()
                            .setGlobalReference(state.eventReference).build())
                    .addExecutionReference(ReferenceWithMetaExecution.ReferenceWithMetaExecutionBuilder()
                            .setGlobalReference(state.execution().meta.globalKey).build())

                    .build())
            .setStatus(AffirmationStatusEnum.AFFIRMED)

    val partyRole = state.execution().partyRole.first { it.role == PartyRoleEnum.CLIENT }
    val partyRef = partyRole.partyReference.globalReference
    val party = state.execution().party.first { it.globalReference == partyRef }

    affirmationBuilder
            .addIdentifier(Identifier.IdentifierBuilder()
                    .addAssignedIdentifier(AssignedIdentifier.AssignedIdentifierBuilder()
                            .setIdentifier(FieldWithMetaString.FieldWithMetaStringBuilder().setValue(UniqueIdentifier().id.toString().replace("-", ""))
                                    .build())
                            .setVersion(1)
                            .build())
                    .setIssuerReference(ReferenceWithMetaParty.ReferenceWithMetaPartyBuilder().setGlobalReference(partyRef).build())
                    .setMeta(MetaFields.MetaFieldsBuilder().setGlobalKey(UniqueIdentifier().id.toString().replace("-", "")).build())
                    .build())
            .addParty(party.value)

    state.execution().partyRole
            .filter { it.partyReference.globalReference == partyRef }
            .forEach { affirmationBuilder.addPartyRole(it) }

    return affirmationBuilder.build()
}
