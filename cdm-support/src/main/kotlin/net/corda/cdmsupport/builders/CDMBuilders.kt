import com.fasterxml.jackson.databind.ObjectMapper
import com.regnosys.rosetta.common.hashing.*
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.process.BuilderProcessor
import com.rosetta.model.lib.process.PostProcessStep
import com.rosetta.model.lib.records.Date
import com.rosetta.model.lib.records.DateImpl
import net.corda.cdmsupport.eventparsing.parseEventFromJson
import net.corda.cdmsupport.eventparsing.serializeCdmObjectIntoJson
import org.isda.cdm.*
import org.isda.cdm.metafields.*
import org.isda.cdm.rosettakey.SerialisingHashFunction
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.function.Supplier

fun main(args: Array<String>) {

    val builder = CDMBuilders()
    val buildAffirmation = builder.affirmation

    println(serializeCdmObjectIntoJson(buildAffirmation))

}

open class CDMBuilders {

    var allocationEvent = parseEventFromJson(this.javaClass.getResource("UC2_Allocation_Trade_AT1.json").readText())

    val allocationExecutions = allocationEvent.primitive.allocation.flatMap { it.after.allocatedTrade.map { trade -> trade.execution } }
    val clientReferences = partyReferenceByRole(allocationExecutions[0].partyRole, PartyRoleEnum.CLIENT)
    val clients = partiesByReference(allocationEvent.party, clientReferences)
    val clientRoles = partyRoleByReference(allocationExecutions[0].partyRole, clientReferences)
    val lineage = buildLineage(allocationEvent.meta.globalKey, allocationExecutions[0].meta.globalKey)
    val affirmationBuilder = buildAffirmation("1", clients, clientRoles, lineage)

    val affirmation = affirmationBuilder.build()

    fun executionAddParty(execution: Execution, parties: MutableSet<Party>): Execution {
        val executionBuilder = execution.toBuilder()
        parties.forEach {
            executionBuilder.addParty(ReferenceWithMetaParty.ReferenceWithMetaPartyBuilder()
                    .setValue(it)
                    .setGlobalReference(it.meta.globalKey)
                    .build()).build()
        }
        return executionBuilder.build()
    }

    fun getExecutionFromEvent(event: Event): Execution {
        return event.primitive.execution.map { it.after.execution }.single()
    }

    fun buildParty(partyId: String, partyName: String, externalKey: String): Party {
        var party = Party.PartyBuilder()
                .addPartyId(buildFieldWithMetaString(partyId))
                .setName(buildFieldWithMetaString(partyName))
                .build()
        party = party.toBuilder().setMeta(buildMetaFields(hash(party), externalKey)).build()
        return party
    }

    fun buildParty(account: Account, partyName: String, partyId: String): Party {
        var party = Party.PartyBuilder()
                .addPartyId(buildFieldWithMetaString(partyId))
                .setName(buildFieldWithMetaString(partyName))
                .setAccount(account)
                .build()
        party = party.toBuilder().setMeta(buildMetaFields(hash(party), partyId)).build()
        return party
    }

    fun buildAccount(accountName: String, accountNumber: String): Account {
        return Account.AccountBuilder()
                .setAccountName(buildFieldWithMetaString(accountName))
                .setAccountNumber(buildFieldWithMetaString(accountNumber))
                .build()
    }

    fun buildFieldWithMetaString(value: String): FieldWithMetaString {
        return FieldWithMetaString.FieldWithMetaStringBuilder().setValue(value).build()
    }

    fun buildFieldWithMetaDate(date: Date): FieldWithMetaDate {
        return FieldWithMetaDate.FieldWithMetaDateBuilder()
                .setValue(date)
                .build()
    }

    fun buildMetaFields(hash: String, externalKey: String): MetaFields {
        return MetaFields.builder().setGlobalKey(hash).setExternalKey(externalKey).build()
    }

    fun <T : RosettaModelObject> hash(value: T): String {
        return SerialisingHashFunction().hash(value)
    }

    fun buildReferenceWithMetaParty(party: Party): ReferenceWithMetaParty {
        return ReferenceWithMetaParty.ReferenceWithMetaPartyBuilder()
                .setGlobalReference(party.meta.globalKey)
//            .setExternalReference(party.meta.externalKey)
//            .setValue(party)
                .build()
    }

    fun buildReferenceWithMetaParty(parties: Collection<Party>): MutableList<ReferenceWithMetaParty> {
        return parties.map { buildReferenceWithMetaParty(it) }.toMutableList()
    }

    fun buildReferenceWithMetaAccount(account: Account): ReferenceWithMetaAccount {
        return ReferenceWithMetaAccount.builder()
                .setGlobalReference(account.meta.globalKey)
//            .setExternalReference(account.meta.externalKey)
//            .setValue(account)
                .build()
    }

    fun readFileDirectlyAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)


    fun partyByPartyRoleGlobalReference(partyRoles: MutableList<PartyRole>, parties: MutableList<org.isda.cdm.Party>): MutableSet<org.isda.cdm.Party> {
        val result = mutableSetOf<org.isda.cdm.Party>()
        partyRoles.forEach { partyRole: PartyRole? -> result.addAll(parties.filter { partyRole?.partyReference?.globalReference == it.meta.globalKey }) }
        return result
    }

    fun partyReferenceByRole(partyRoles: Collection<PartyRole>, partyRole: PartyRoleEnum): MutableSet<String> {
        return partyRoles.filter { it.role == partyRole }.map { it.partyReference.globalReference }.toMutableSet()
    }

    fun partiesByReference(parties: Collection<Party>, references: MutableSet<String>): MutableList<Party> {
        return parties.filter { references.contains(it.meta.globalKey) }.toMutableList()
    }

    fun partyRoleByReference(partyRoles: Collection<PartyRole>, references: Collection<String>): MutableList<PartyRole> {
        return partyRoles.filter { references.contains(it.partyReference.globalReference) }.toMutableList()
    }

    fun buildLineage(eventKey: String, executionKey: String): Lineage {
        return Lineage.LineageBuilder()
                .addEventReferenceBuilder(ReferenceWithMetaEvent
                        .ReferenceWithMetaEventBuilder()
                        .setGlobalReference(eventKey))
                .addExecutionReferenceBuilder(ReferenceWithMetaExecution
                        .ReferenceWithMetaExecutionBuilder()
                        .setGlobalReference(executionKey)
                ).build()
    }

    fun buildReferenceWithMetaExecution(execution: Execution): ReferenceWithMetaExecution {
        return ReferenceWithMetaExecution.ReferenceWithMetaExecutionBuilder()
                .setGlobalReference(execution.meta.globalKey)
//            .setValue(execution)
                .build()
    }

    fun buildAffirmation(tradeIndex: String, parties: MutableList<Party>, partyRoles: MutableList<PartyRole>, lineage: Lineage): Affirmation.AffirmationBuilder {
        val builder = Affirmation.AffirmationBuilder()
                .addParty(parties)
                .addPartyRole(partyRoles)
                .setLineage(lineage)
                .setStatus(AffirmationStatusEnum.AFFIRMED)

        val identifier = SerialisingHashFunction().hash(builder.build())

        builder.addIdentifierBuilder(Identifier
                .IdentifierBuilder()
                .addAssignedIdentifierBuilder(AssignedIdentifier
                        .AssignedIdentifierBuilder()
                        .setIdentifier(FieldWithMetaString.builder().setValue(identifier).build())
                        .setVersion(tradeIndex.toInt())
                ))

        return builder
    }

    fun buildConfirmation(tradeIndex: String, parties: MutableList<Party>, partyRoles: MutableList<PartyRole>, lineage: Lineage): Confirmation.ConfirmationBuilder {
        return Confirmation.ConfirmationBuilder()
                .addIdentifierBuilder(Identifier
                        .IdentifierBuilder()
                        .addAssignedIdentifierBuilder(AssignedIdentifier
                                .AssignedIdentifierBuilder()
                                .setIdentifier(buildFieldWithMetaString(tradeIndex))
                        ))
                .addParty(parties)
                .addPartyRole(partyRoles)
                .setLineage(lineage)
                .setStatus(ConfirmationStatusEnum.CONFIRMED)
    }

    fun buildCashTransfer(amount: Money, payerReceiver: PayerReceiver, identifier: FieldWithMetaString): CashTransferComponent {
        return CashTransferComponent.builder()
                .setAmount(amount)
                .setPayerReceiver(payerReceiver)
                .setIdentifier(identifier).build()
    }

    fun buildSecurity(bond: Bond): Security {
        return Security.SecurityBuilder()
                .setBond(bond)
                .build()
    }

    fun buildBond(productIdentifier: ProductIdentifier): Bond {
        return Bond.BondBuilder()
                .setProductIdentifier(productIdentifier)
                .build()
    }

    fun buildProductIdentifier(identifier: String, productIdSourceEnum: ProductIdSourceEnum): ProductIdentifier {
        return ProductIdentifier.ProductIdentifierBuilder()
                .addIdentifier(buildFieldWithMetaString(identifier))
                .setSource(productIdSourceEnum)
                .build()
    }

    fun buildProduct(security: Security): Product {
        return Product.ProductBuilder()
                .setSecurity(security)
                .build()
    }

    fun buildIdentifier(identifier: String, issuerReference: Party): Identifier {
        return Identifier.IdentifierBuilder()
                .addAssignedIdentifier(buildAssignedIdentifier(identifier, 1))
                .setIssuerReference(buildReferenceWithMetaParty(issuerReference))
                .build()
    }

    fun buildAssignedIdentifier(identifier: String, version: Int): AssignedIdentifier {
        return AssignedIdentifier.AssignedIdentifierBuilder().setIdentifier(buildFieldWithMetaString(identifier)).setVersion(version).build()
    }

    fun buildPartyRole(role: PartyRoleEnum, party: Party): PartyRole {
        return PartyRole.PartyRoleBuilder()
                .setRole(role)
                .setPartyReference(buildReferenceWithMetaParty(party))
                .build()
    }

    fun buildPrice(accruedInterest: BigDecimal, grossAmount: BigDecimal, grossCurrency: String, netAmount: BigDecimal, netcurrency: String, priceExpression: PriceExpressionEnum): Price {
        return Price.PriceBuilder()
                .setAccruedInterest(accruedInterest)
                .setGrossPrice(buildActualPrice(grossAmount, grossCurrency, priceExpression))
                .setNetPrice(buildActualPrice(netAmount, netcurrency, priceExpression))
                .build()
    }

    fun buildActualPrice(amount: BigDecimal, currency: String, priceExpression: PriceExpressionEnum): ActualPrice {
        return ActualPrice.ActualPriceBuilder()
                .setAmount(amount)
                .setCurrency(buildFieldWithMetaString(currency))
                .setPriceExpression(priceExpression)
                .build()
    }


    fun buildPayerReceiver(payer: Party, receiver: Party): PayerReceiver {
        return PayerReceiver.PayerReceiverBuilder()
                .setPayerAccountReference(buildReferenceWithMetaAccount(payer.account))
                .setPayerPartyReference(buildReferenceWithMetaParty(payer))
                .setReceiverAccountReference(buildReferenceWithMetaAccount(receiver.account))
                .setReceiverPartyReference(buildReferenceWithMetaParty(receiver)).build()
    }

    fun buildTransferorTransferee(transferee: Party, transferor: Party): TransferorTransferee {
        return TransferorTransferee.TransferorTransfereeBuilder()
                .setTransfereeAccountReference(buildReferenceWithMetaAccount(transferee.account))
                .setTransfereePartyReference(buildReferenceWithMetaParty(transferee))
                .setTransferorAccountReference(buildReferenceWithMetaAccount(transferor.account))
                .setTransferorPartyReference(buildReferenceWithMetaParty(transferor))
                .build()
    }

    fun buildAmount(amount: BigDecimal, currency: String): Money {
        return Money.MoneyBuilder()
                .setAmount(amount).setCurrency(buildFieldWithMetaString(currency))
                .build()
    }

    fun buildQuantity(amount: BigDecimal): Quantity {
        return Quantity.QuantityBuilder()
                .setAmount(amount)
                .build()
    }

    fun buildQuantity(amount: BigDecimal, currency: String): Quantity {
        return Quantity.QuantityBuilder()
                .setAmount(amount)
                .setCurrency(buildFieldWithMetaString(currency))
                .build()
    }

    fun buildSettlementTerms(settlementAmount: Money, settlementDate: AdjustableOrRelativeDate): SettlementTerms {
        return SettlementTerms.SettlementTermsBuilder()
                .setSettlementAmount(settlementAmount)
                .setSettlementDate(settlementDate)
                .build()
    }

    fun buildMoney(amount: BigDecimal, currency: String): Money {
        return Money.MoneyBuilder()
                .setAmount(amount)
                .setCurrency(buildFieldWithMetaString("currency"))
                .build()
    }

    fun buildAdjustableOrRelativeDate(adjustableDate: AdjustableDate): AdjustableOrRelativeDate {
        return AdjustableOrRelativeDate.AdjustableOrRelativeDateBuilder()
                .setAdjustableDate(adjustableDate)
                .build()
    }

    fun buildAdjustableDate(unadjustedDate: Date): AdjustableDate {
        return AdjustableDate.AdjustableDateBuilder().setUnadjustedDate(unadjustedDate).build()
    }

    fun buildSecurityTransfer(quantity: BigDecimal, security: Security, transferorTransferee: TransferorTransferee): SecurityTransferComponent {
        return SecurityTransferComponent.builder()
                .setQuantity(quantity)
                .setSecurity(security)
                .setTransferorTransferee(transferorTransferee).build()
    }

    fun buildTransferorTransferee(transfereeAccountReference: ReferenceWithMetaAccount, transfereePartyReference: ReferenceWithMetaParty, transferorAccountReference: ReferenceWithMetaAccount, transferorPartyReference: ReferenceWithMetaParty): TransferorTransferee {
        return TransferorTransferee.builder()
                .setTransfereeAccountReference(transfereeAccountReference)
                .setTransfereePartyReference(transfereePartyReference)
                .setTransferorAccountReference(transferorAccountReference)
                .setTransferorPartyReference(transferorPartyReference).build()
    }

    fun buildLegalEntity(name: String): LegalEntity {
        return LegalEntity.LegalEntityBuilder().setName(buildFieldWithMetaString(name)).build()
    }

    fun buildEventEffect(executions: Collection<Execution>): EventEffect {
        return EventEffect.EventEffectBuilder()
                .addEffectedExecution(executions.map { buildReferenceWithMetaExecution(it) })
                .build()
    }

    fun buildEventTimeStamp(dataTime: ZonedDateTime, qualification: EventTimestampQualificationEnum): EventTimestamp {
        return EventTimestamp.EventTimestampBuilder()
                .setDateTime(dataTime)
                .setQualification(qualification)
                .build()
    }

    fun buildPrimitiveEvent(executionPrimitive: ExecutionPrimitive): PrimitiveEvent {
        return PrimitiveEvent.PrimitiveEventBuilder()
                .addExecution(executionPrimitive)
                .build()
    }

    fun buildPrimitiveEvent(allocationPrimitive: AllocationPrimitive): PrimitiveEvent {
        return PrimitiveEvent.PrimitiveEventBuilder()
                .addAllocation(allocationPrimitive)
                .build()
    }

    fun buildExecutionPrimitive(afterExecution: Execution): ExecutionPrimitive {
        val builder = ExecutionState.ExecutionStateBuilder().setExecution(afterExecution)
        return ExecutionPrimitive.ExecutionPrimitiveBuilder()
                .setAfter(builder.build())
                .build()
    }

    fun buildAllocationPrimitive(after: AllocationOutcome, before: Trade): AllocationPrimitive {
        return AllocationPrimitive.AllocationPrimitiveBuilder()
                .setAfter(after)
                .setBefore(before)
                .build()
    }

    fun buildAllocationTrade(execution: Execution): Trade {
        return Trade.TradeBuilder()
                .setExecution(execution)
                .build()
    }

    fun buildAllocationTrade(executions: Collection<Execution>): MutableList<Trade> {
        return executions.map { buildAllocationTrade(it) }.toMutableList()
    }

    fun buildAllocationOutcome(allocationTrade: List<Trade>, originalTrade: Trade): AllocationOutcome {
        return AllocationOutcome.AllocationOutcomeBuilder()
                .addAllocatedTrade(allocationTrade)
                .setOriginalTrade(originalTrade)
                .build()
    }

    fun buildExecutionState(beforeExecution: Execution): ExecutionState {
        return ExecutionState.ExecutionStateBuilder().setExecution(beforeExecution).build()
    }

    fun buildAllocationInstruction(allocationInstructions: String): AllocationInstructions {

        val mapper = ObjectMapper()
        val items = mapper.readTree(allocationInstructions)
        val allocations = items.flatMap { it.get("Allocations") }

        val allocationInstructionsBuilder = AllocationInstructions.AllocationInstructionsBuilder()
        allocations.forEach {
            val account = buildAccount(it.get("ClientAccount").get("Account").get("accountName").toString(), it.get("ClientAccount").get("Account").get("accountNumber").toString())
            val party = buildParty(account, it.get("ClientAccount").get("Party").get("name").toString(), it.get("ClientAccount").get("Party").get("partyId").toString())
            allocationInstructionsBuilder.addBreakdowns(buildAllocationBreakDown(buildQuantity(BigDecimal(it.get("Quantity").toString())), party))
        }

        return allocationInstructionsBuilder.build()
    }

    fun buildAllocationBreakDown(quantity: Quantity, party: Party): AllocationBreakdown {
        return AllocationBreakdown.AllocationBreakdownBuilder()
                .setQuantity(quantity)
                .setPartyReference(buildReferenceWithMetaParty(party))
                .build()
    }

    fun <T : RosettaModelObject> process(aClass: Class<T>, builder: RosettaModelObjectBuilder) {
        val postProcessors: List<PostProcessStep>
        val rosettaKeyProcessStep = RosettaKeyProcessStep(Supplier<BuilderProcessor> { NonNullHashCollector() })
        postProcessors = listOf(rosettaKeyProcessStep,
                RosettaKeyValueProcessStep(Supplier<BuilderProcessor> { RosettaKeyValueHashFunction() }),
                ReKeyProcessStep(rosettaKeyProcessStep))

        postProcessors.forEach { postProcessStep -> postProcessStep.runProcessStep(aClass, builder) }
    }

    fun buildExecutionEvent(execution: Execution, action: ActionEnum, eventDate: LocalDate, identifier: String, issuerReference: Party, parties: MutableList<Party>, primitive: PrimitiveEvent): Event {

        val builder = Event.EventBuilder()
                .setAction(action)
                .setEventDate(DateImpl(eventDate))
                .setEventEffect(buildEventEffect(listOf(execution)))
                .addEventIdentifier(buildIdentifier(identifier, issuerReference))
                .addParty(parties)
                .setPrimitive(primitive)
                .addTimestamp(buildEventTimeStamp(ZonedDateTime.now(), EventTimestampQualificationEnum.EVENT_CREATION_DATE_TIME))
        process(Event::class.java, builder)
        return builder.build()

    }

    fun buildAllocationEvent(execution: List<Execution>, action: ActionEnum, eventDate: LocalDate, identifier: String, lineage: Lineage, issuerReference: Party, parties: MutableList<Party>, primitive: PrimitiveEvent): Event {

        //TODO
        return Event.EventBuilder().build()

    }

    fun buildParties(): MutableMap<String, Party> {

        val account1 = buildAccount("Client1_ACT#2", "Client1_ACT#2_BJKXFTGW4BFPY")
        val client1 = buildParty(account1, "Client1", "Client1_ID#2_CAOIBGBYU6QPR")

        val account2 = buildAccount("Broker1_ACT#0", "Broker1_ACT#0_WWVA12ZJ21IW2")
        val client2 = buildParty(account2, "Broker1", "Broker1_ID#0_4NGIDYZJ4ZBDX")

        val account3 = buildAccount("Broker2_ACT#0", "Broker2_ACT#0_TTJE0NQUTRIFB")
        val client3 = buildParty(account3, "Broker2", "Broker2_ID#0_AN93QUDTMRPAB")

        return mutableMapOf<String, Party>("Client1" to client1, "Broker1" to client2, "Broker2" to client3)
    }

    fun buildRoles(parties: MutableMap<String, Party>): MutableList<PartyRole> {

        return mutableListOf<PartyRole>(
                buildPartyRole(PartyRoleEnum.CLIENT, parties.getValue("Client1")),
                buildPartyRole(PartyRoleEnum.EXECUTING_ENTITY, parties.getValue("Broker1")),
                buildPartyRole(PartyRoleEnum.BUYER, parties.getValue("Client1")),
                buildPartyRole(PartyRoleEnum.BUYER, parties.getValue("Broker1")),
                buildPartyRole(PartyRoleEnum.COUNTERPARTY, parties.getValue("Broker2")),
                buildPartyRole(PartyRoleEnum.SELLER, parties.getValue("Broker2"))
        )
    }


    fun buildExecution(parties: MutableMap<String, Party>, roles: MutableList<PartyRole>): Execution {

        val execution = Execution.ExecutionBuilder()
                .setExecutionType(ExecutionTypeEnum.ELECTRONIC)
                .setExecutionVenue(buildLegalEntity("Execution Venue"))
                .addIdentifier(buildIdentifier("W3S0XZGEM4S82", parties.get("Client1")!!))
                .addPartyRole(roles)
                .addParty(buildReferenceWithMetaParty(parties.values))
                .setPrice(buildPrice(BigDecimal("1.3700"), BigDecimal("96.9700"), "USD", BigDecimal("98.3400"), "USD", PriceExpressionEnum.ABSOLUTE_TERMS))
                .setProduct(buildProduct(buildSecurity(buildBond(buildProductIdentifier("DH0371475458", ProductIdSourceEnum.CUSIP)))))
                .setQuantity(buildQuantity(BigDecimal("800000.00")))
                .setSettlementTerms(
                        buildSettlementTerms(
                                buildAmount(BigDecimal("786720.00"), "USD"),
                                buildAdjustableOrRelativeDate(buildAdjustableDate(DateImpl(LocalDate.of(2019, 10, 17))))))
                .setTradeDate(buildFieldWithMetaDate(DateImpl(LocalDate.of(2019, 10, 16))))

        process(Execution::class.java, execution)
        return execution.build()
    }

    fun buildExecutionEvent(execution: Execution, parties: MutableMap<String, Party>): Event {
        val listParties = parties.map { it.value }.toMutableList<Party>()
        val executionPrimitive = buildExecutionPrimitive(execution)
        val eventPrimitiveEvent = buildPrimitiveEvent(executionPrimitive)
        val event = buildExecutionEvent(execution, ActionEnum.NEW, LocalDate.of(2019, 10, 16), "NZVJ31U4568YT", parties.get("Broker1")!!, listParties, eventPrimitiveEvent)
        return event
    }

    fun buildPortfolioInstruction(portfolioInstructions: String) : PortfolioInstructions {

        val mapper = ObjectMapper()
        val items = mapper.readTree(portfolioInstructions)
        val portfolio = items.get("PortfolioInstructions")

        val rosettaObjectMapper = RosettaObjectMapper.getDefaultRosettaObjectMapper()
        val party = rosettaObjectMapper.readValue<Party>(portfolio.get("Client").toString(), Party::class.java)
        val security = rosettaObjectMapper.readValue<Security>(portfolio.get("security").toString(), Security::class.java)
        val date = rosettaObjectMapper.readValue<Date>(portfolio.get("PortfolioDate").toString(), Date::class.java)

        return PortfolioInstructions(date, party, security)
    }
}



