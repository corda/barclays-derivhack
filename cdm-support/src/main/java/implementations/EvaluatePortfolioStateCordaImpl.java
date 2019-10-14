package implementations;

import com.google.common.collect.MoreCollectors;
import com.regnosys.rosetta.common.hashing.*;
import com.rosetta.model.lib.process.PostProcessStep;
import net.corda.core.serialization.CordaSerializable;
import org.isda.cdm.*;
import org.isda.cdm.functions.EvaluatePortfolioState;
import org.isda.cdm.metafields.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CordaSerializable
public class EvaluatePortfolioStateCordaImpl extends EvaluatePortfolioState {
    private final List<Execution> executions;
    private final List<PostProcessStep> postProcessors;

    public EvaluatePortfolioStateCordaImpl(List<Execution> executions) {
        this.executions = executions;
        RosettaKeyProcessStep rosettaKeyProcessStep = new RosettaKeyProcessStep(NonNullHashCollector::new);
        this.postProcessors = Arrays.asList(rosettaKeyProcessStep, new RosettaKeyValueProcessStep(RosettaKeyValueHashFunction::new), new ReKeyProcessStep(rosettaKeyProcessStep));
    }

    @NotNull
    protected PortfolioState.PortfolioStateBuilder doEvaluate(Portfolio input) {
        AggregationParameters params = input.getAggregationParameters();
        LocalDate date = params.getDateTime().toLocalDate();
        boolean totalPosition = (Boolean) Optional.ofNullable(params.getTotalPosition()).orElse(false);
        List<Execution> filteredExecution = this.executions.stream().filter((e) -> {
            return this.filterByDate(e, date, totalPosition);
        }).filter((e) -> {
            return this.filterByPositionStatus(e, date, params.getPositionStatus());
        }).filter((e) -> {
            return this.filterByProducts(e, params.getProduct());
        }).filter((e) -> {
            return this.filterByParty(e, params.getParty());
        }).collect(Collectors.toList());
        Map<Position, BigDecimal> positionQuantity = filteredExecution.stream().collect(Collectors.groupingBy((e) -> {
            return this.toPosition(e, date);
        }, Collectors.reducing(BigDecimal.ZERO, this::getAggregationQuantity, BigDecimal::add)));
        //Created additionally
        Map<Position, BigDecimal> positionCashBalance = filteredExecution.stream().collect(Collectors.groupingBy((e) -> {
            return this.toPosition(e, date);
        }, Collectors.reducing(BigDecimal.ZERO, this::getAggregationSettlementAmount, BigDecimal::add)));
        Set<Position> aggregatedPositions = positionQuantity.keySet().stream().map((p) -> {
            return p.toBuilder().setQuantityBuilder(Quantity.builder().setAmount((BigDecimal)positionQuantity.get(p))).setCashBalanceBuilder(Money.builder().setAmount((BigDecimal)positionCashBalance.get(p)).setCurrency(FieldWithMetaString.builder().setValue("USD").build())).build();
        }).collect(Collectors.toSet());
        PortfolioState.PortfolioStateBuilder portfolioStateBuilder = PortfolioState.builder();
        Objects.requireNonNull(portfolioStateBuilder);
        aggregatedPositions.forEach(portfolioStateBuilder::addPositions);
        this.postProcessors.forEach((postProcessStep) -> {
            postProcessStep.runProcessStep(PortfolioState.class, portfolioStateBuilder);
        });
        portfolioStateBuilder.setLineage(Lineage.builder()
                .addPortfolioStateReference(ReferenceWithMetaPortfolioState.builder().setValue(input.getPortfolioState()).build())
                .addEventReference(input.getPortfolioState().getLineage().getEventReference())
                .addExecutionReference(filteredExecution.stream()
                        .map(e -> ReferenceWithMetaExecution.builder().setGlobalReference(e.getMeta().getGlobalKey()).build()).collect(Collectors.toList())).build());

        return portfolioStateBuilder;
    }

    private boolean filterByDate(Execution execution, LocalDate date, boolean totalPosition) {
        return this.filterByTradeDate(execution, date, totalPosition) || this.filterBySettlementDate(execution, date, totalPosition);
    }

    private boolean filterByTradeDate(Execution execution, LocalDate date, boolean totalPosition) {
        return (Boolean)Optional.ofNullable(execution.getTradeDate()).map(FieldWithMetaDate::getValue).map((tradeDate) -> {
            return this.matches(date, tradeDate.toLocalDate(), totalPosition);
        }).orElseThrow(() -> {
            return new RuntimeException(String.format("Trade date not set on execution [%s]", execution));
        });
    }

    private boolean filterBySettlementDate(Execution execution, LocalDate date, boolean totalPosition) {
        return (Boolean)Optional.ofNullable(execution.getSettlementTerms()).map(SettlementTerms::getSettlementDate).map(AdjustableOrRelativeDate::getAdjustableDate).map(AdjustableDate::getUnadjustedDate).map((settlementDate) -> {
            return this.matches(date, settlementDate.toLocalDate(), totalPosition);
        }).orElse(true);
    }

    private boolean matches(LocalDate dateToFind, LocalDate tradeDate, boolean totalPosition) {
        return totalPosition ? dateToFind.compareTo(tradeDate) >= 0 : dateToFind.compareTo(tradeDate) == 0;
    }

    private boolean filterByPositionStatus(Execution execution, LocalDate date, PositionStatusEnum positionStatusToFind) {
        return positionStatusToFind == null ? true : (Boolean)Optional.ofNullable(execution.getTradeDate()).map(FieldWithMetaDate::getValue).map((tradeDate) -> {
            return this.toPositionStatus(execution, date);
        }).map((s) -> {
            return s == positionStatusToFind;
        }).orElseThrow(() -> {
            return new RuntimeException(String.format("Trade date not set on execution [%s]", execution));
        });
    }

    private boolean filterByProducts(Execution execution, List<Product> productsToFind) {
        if (productsToFind != null && !productsToFind.isEmpty()) {
            ProductIdentifier productIdentifier = (ProductIdentifier)Optional.ofNullable(execution.getProduct()).map(Product::getSecurity).map(Security::getBond).map(IdentifiedProduct::getProductIdentifier).orElseThrow(() -> {
                return new RuntimeException(String.format("ProductIdentifier not set on execution [%s]", execution));
            });
            Set<String> identifiers = this.getIdentifiersAsString(productIdentifier);
            ProductIdSourceEnum source = productIdentifier.getSource();
            return productsToFind.stream().map(Product::getSecurity).map(Security::getBond).map(IdentifiedProduct::getProductIdentifier).anyMatch((idsToFind) -> {
                Stream var10000 = this.getIdentifiersAsString(idsToFind).stream();
                Objects.requireNonNull(identifiers);
                return var10000.anyMatch(identifiers::contains) && source == idsToFind.getSource();
            });
        } else {
            return true;
        }
    }

    private Set<String> getIdentifiersAsString(ProductIdentifier productIdentifier) {
        return (Set)productIdentifier.getIdentifier().stream().map(FieldWithMetaString::getValue).collect(Collectors.toSet());
    }

    private boolean filterByParty(Execution execution, List<ReferenceWithMetaParty> partyToFind) {
        if (partyToFind != null && !partyToFind.isEmpty()) {
            List<String> partyIdToFind = (List)partyToFind.stream().map(ReferenceWithMetaParty::getValue).map(Party::getPartyId).flatMap(Collection::stream).map(FieldWithMetaString::getValue).collect(Collectors.toList());
            Stream var10000 = execution.getParty().stream().map(ReferenceWithMetaParty::getValue).map(Party::getPartyId).flatMap(Collection::stream).map(FieldWithMetaString::getValue);
            Objects.requireNonNull(partyIdToFind);
            return var10000.anyMatch(partyIdToFind::contains);
        } else {
            return true;
        }
    }

    private Position toPosition(Execution execution, LocalDate date) {
        Quantity quantity = execution.getQuantity();
        if (quantity.getUnit() != null) {
            throw new IllegalArgumentException("Position aggregation not supported for quantities with units " + quantity.getUnit().name());
        } else {
            Position.PositionBuilder positionBuilder = Position.builder().setPositionStatus(this.toPositionStatus(execution, date)).setProduct(execution.getProduct());
            this.postProcessors.forEach((postProcessStep) -> {
                postProcessStep.runProcessStep(Position.class, positionBuilder);
            });
            return positionBuilder.build();
        }
    }

    private PositionStatusEnum toPositionStatus(Execution execution, LocalDate date) {
        Optional<ClosedState> closedState = Optional.ofNullable(execution.getClosedState());
        if ((Boolean)closedState.map((s) -> {
            return s.getState() == ClosedStateEnum.CANCELLED;
        }).orElse(false) && (Boolean)closedState.map(ClosedState::getActivityDate).map((d) -> {
            return date.isAfter(d.toLocalDate());
        }).orElse(false)) {
            return PositionStatusEnum.CANCELLED;
        } else if ((Boolean)Optional.ofNullable(execution.getSettlementTerms()).map(SettlementTerms::getSettlementDate).map(AdjustableOrRelativeDate::getAdjustableDate).map(AdjustableDate::getUnadjustedDate).map((settlementDate) -> {
            return date.compareTo(settlementDate.toLocalDate()) >= 0;
        }).orElse(false)) {
            return PositionStatusEnum.SETTLED;
        } else if ((Boolean)Optional.ofNullable(execution.getTradeDate()).map(FieldWithMetaDate::getValue).map((tradeDate) -> {
            return date.compareTo(tradeDate.toLocalDate()) >= 0;
        }).orElse(false)) {
            return PositionStatusEnum.EXECUTED;
        } else {
            throw new RuntimeException(String.format("Unable to determine PositionStatus on date [%s] for execution [%s]", date, execution));
        }
    }

    private BigDecimal getAggregationQuantity(Execution e) {
        PartyRoleEnum buyOrSell = this.getExecutingEntityBuyOrSell(e);
        BigDecimal quantity = e.getQuantity().getAmount();
        return buyOrSell == PartyRoleEnum.SELLER ? quantity.negate() : quantity;
    }

    private BigDecimal getAggregationSettlementAmount(Execution e) {
        PartyRoleEnum buyOrSell = this.getExecutingEntityBuyOrSell(e);
        BigDecimal settlementAmount = e.getSettlementTerms().getSettlementAmount().getAmount();
        return buyOrSell == PartyRoleEnum.SELLER ? settlementAmount : settlementAmount.negate();
    }

    private PartyRoleEnum getExecutingEntityBuyOrSell(Execution e) {
        String partyReference = (String)e.getPartyRole().stream().filter((r) -> {
            return r.getRole() == PartyRoleEnum.EXECUTING_ENTITY;
        }).map((r) -> {
            return r.getPartyReference().getGlobalReference();
        }).collect(MoreCollectors.onlyElement());
        return (PartyRoleEnum)e.getPartyRole().stream().filter((r) -> {
            return partyReference.equals(r.getPartyReference().getGlobalReference());
        }).map(PartyRole::getRole).filter((r) -> {
            return r == PartyRoleEnum.BUYER || r == PartyRoleEnum.SELLER;
        }).collect(MoreCollectors.onlyElement());
    }
}
