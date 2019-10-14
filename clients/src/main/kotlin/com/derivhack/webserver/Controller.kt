package com.derivhack.webserver

import com.derivhack.*
import com.derivhack.webserver.helpers.checkExecutionIsAffirmed
import com.derivhack.webserver.models.binding.PortfolioBindingModel
import com.derivhack.webserver.models.view.*
import net.corda.cdmsupport.states.*
import net.corda.core.messaging.FlowHandle
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.transactions.SignedTransaction
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @PostMapping(value = ["/execution"])
    private fun execution(@RequestBody executionJson: String): String {

        val tx = proxy.startFlowDynamic(ExecutionFlow::class.java, executionJson)

        return "Transaction with id: ${tx.id} created"
    }

    @PostMapping(value = ["/allocation"])
    @Throws(java.lang.RuntimeException::class)
    private fun allocation(@RequestBody allocationJson: String): String {

        val tx = proxy.startFlowDynamic(AllocationFlow::class.java, allocationJson)

        return "Transaction with id: ${tx.id} created"
    }

    @PostMapping(value = ["/affirmation"])
    private fun affirmation(@RequestParam executionRef: String): String {

        val tx = proxy.startFlowDynamic(AffirmationFlow::class.java, executionRef)

        return "Transaction with id: ${tx.id} created"
    }

    @PostMapping(value = ["/confirmation"])
    private fun confirmation(@RequestParam executionRef: String): String {

        lateinit var tx : FlowHandle<Unit>

        if (checkExecutionIsAffirmed(executionRef, proxy)) {
            tx = proxy.startFlowDynamic(ConfirmationFlow::class.java, executionRef)
        }

        return "Transaction with id: ${tx.id} created"
    }


    @PostMapping(value = ["/portfolio"])
    private fun portfolio(@RequestBody portfolio: PortfolioBindingModel): String {

        val tx = proxy.startFlowDynamic(PortfolioFlow::class.java, portfolio.transferRefs, portfolio.executionRefs, portfolio.pathToInstructions)

        return "Transaction with id: ${tx.id} created"
    }


    @PostMapping(value = ["/transfer"])
    private fun transfer(@RequestBody transferJson: String): String {

        val tx = proxy.startFlowDynamic(TransferFlow::class.java, transferJson)

        return "Transaction with id: ${tx.id} created"
    }

    @GetMapping(value = ["/execution-states"])
    private fun executionStates(): List<ExecutionViewModel> {
        val allExecutionStatesAndRefs = proxy.vaultQueryBy<ExecutionState>().states
        val states = allExecutionStatesAndRefs.map { it.state.data }

        return states.map {
            ExecutionViewModel(it.linearId.id.toString(), it.participants, it.execution(), it.eventReference, it.workflowStatus)
        }
    }

    @GetMapping(value = ["/affirmation-states"])
    private fun affirmationStates(): List<AffirmationViewModel> {
        val allAffirmationStatesAndRefs = proxy.vaultQueryBy<AffirmationState>().states
        val states = allAffirmationStatesAndRefs.map { it.state.data }

        return states.map {
            AffirmationViewModel(it.linearId.id.toString(), it.participants, it.affirmation())
        }
    }

    @GetMapping(value = ["/confirmation-states"])
    private fun confirmationStates(): List<ConfirmationViewModel> {
        val allConfirmationStatesAndRefs = proxy.vaultQueryBy<ConfirmationState>().states
        val states = allConfirmationStatesAndRefs.map { it.state.data }

        return states.map {
            ConfirmationViewModel(it.linearId.id.toString(), it.participants, it.confirmation())
        }
    }

    @GetMapping(value = ["/transfer-states"])
    private fun transferStates(): List<TransferViewModel> {
        val allTransferStatesAndRefs = proxy.vaultQueryBy<TransferState>().states
        val states = allTransferStatesAndRefs.map { it.state.data }

        return states.map {
            TransferViewModel(it.linearId.id.toString(), it.transfer())
        }

    }

    @GetMapping(value = ["/portfolio-states"])
    private fun portfolioStates(): List<PortfolioViewModel> {
        val allPortfolioStatesAndRefs = proxy.vaultQueryBy<PortfolioState>().states
        val states = allPortfolioStatesAndRefs.map { it.state.data }

        return states.map {
            PortfolioViewModel(it.linearId.id.toString(), it.participants, it.portfolio())
        }
    }
}
