package implementations;

import com.regnosys.rosetta.common.hashing.NonNullHashCollector;
import com.regnosys.rosetta.common.hashing.ReKeyProcessStep;
import com.regnosys.rosetta.common.hashing.RosettaKeyProcessStep;
import com.regnosys.rosetta.common.hashing.RosettaKeyValueHashFunction;
import com.regnosys.rosetta.common.hashing.RosettaKeyValueProcessStep;
import com.rosetta.model.lib.process.PostProcessStep;
import com.rosetta.model.lib.records.DateImpl;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.isda.cdm.AllocationInstructions;
import org.isda.cdm.AllocationPrimitive.AllocationPrimitiveBuilder;
import org.isda.cdm.AssignedIdentifier;
import org.isda.cdm.Event;
import org.isda.cdm.Event.EventBuilder;
import org.isda.cdm.EventTimestamp;
import org.isda.cdm.EventTimestampQualificationEnum;
import org.isda.cdm.Execution;
import org.isda.cdm.Identifier;
import org.isda.cdm.functions.Allocate;
import org.isda.cdm.metafields.FieldWithMetaString;
import org.isda.cdm.metafields.ReferenceWithMetaParty;
import org.isda.cdm.processor.EventEffectProcessStep;

public class AllocateImpl extends Allocate {

  private final List<PostProcessStep> postProcessors;

  public AllocateImpl() {
    RosettaKeyProcessStep rosettaKeyProcessStep = new RosettaKeyProcessStep(
        NonNullHashCollector::new);
    this.postProcessors = Arrays
        .asList(rosettaKeyProcessStep,
            new RosettaKeyValueProcessStep(RosettaKeyValueHashFunction::new),
            new ReKeyProcessStep(rosettaKeyProcessStep),
            new EventEffectProcessStep(rosettaKeyProcessStep));
  }

  protected EventBuilder doEvaluate(
      Execution execution, AllocationInstructions allocationInstructions, Event previousEvent) {
//    EventBuilder eventBuilder = Event.builder();
    EventBuilder eventBuilder = previousEvent.toBuilder();
    Set<ReferenceWithMetaParty> eventParties = new HashSet();
    Iterator var6 = eventBuilder.getPrimitive().getAllocation().iterator();

    while (var6.hasNext()) {
      AllocationPrimitiveBuilder allocationBuilder = (AllocationPrimitiveBuilder) var6.next();
      Set<ReferenceWithMetaParty> blockExecutionParties = new HashSet(
          allocationBuilder.build().getBefore().getExecution().getParty());
      eventParties.addAll(blockExecutionParties);
      allocationBuilder.getBefore().getExecution().clearParty()
          .addParty(this.replacePartyWithReference(blockExecutionParties));
      allocationBuilder.getAfter().getOriginalTrade().getExecution().clearParty()
          .addParty(this.replacePartyWithReference(blockExecutionParties));
      allocationBuilder.getAfter().getAllocatedTrade().forEach((allocatedTradeBuilder) -> {
        List<ReferenceWithMetaParty> allocatedExecutionParties = (List) Optional
            .ofNullable(allocatedTradeBuilder.build().getExecution()).map(Execution::getParty)
            .orElse(
                Collections.emptyList());
        eventParties.addAll(allocatedExecutionParties);
        allocatedTradeBuilder.getExecution().clearParty()
            .addParty(this.replacePartyWithReference(allocatedExecutionParties));
      });
    }

    eventBuilder.addEventIdentifier(this.getIdentifier("allocationEvent1", 1)).setEventDate(
        DateImpl.of(LocalDate.now()))
        .addParty((List) eventParties.stream().map(ReferenceWithMetaParty::getValue).collect(
            Collectors.toList())).addTimestamp(this.getEventCreationTimestamp(ZonedDateTime.now()));
    this.postProcessors.forEach((postProcessStep) -> {
      postProcessStep.runProcessStep(Event.class, eventBuilder);
    });
    return eventBuilder;
  }

  private List<ReferenceWithMetaParty> replacePartyWithReference(
      Collection<ReferenceWithMetaParty> parties) {
    return (List) parties.stream().map((p) -> {
      return ReferenceWithMetaParty.builder()
          .setGlobalReference(p.getValue().getMeta().getGlobalKey())
          .setExternalReference(p.getValue().getMeta().getExternalKey()).build();
    }).collect(Collectors.toList());
  }

  private EventTimestamp getEventCreationTimestamp(ZonedDateTime eventDateTime) {
    return EventTimestamp.builder().setDateTime(eventDateTime).setQualification(
        EventTimestampQualificationEnum.EVENT_CREATION_DATE_TIME).build();
  }

  private Identifier getIdentifier(String id, int version) {
    return Identifier.builder().addAssignedIdentifierBuilder(
        AssignedIdentifier.builder()
            .setIdentifier(FieldWithMetaString.builder().setValue(id).build()).setVersion(version))
        .build();
  }
}
