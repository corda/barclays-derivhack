package com.derivhack.webserver

import com.derivhack.AffirmationFlow
import com.derivhack.AllocationFlow
import com.derivhack.ExecutionFlow
import com.derivhack.webserver.models.AffirmationViewModel
import com.derivhack.webserver.models.ExecutionViewModel
import net.corda.cdmsupport.states.AffirmationState
import net.corda.cdmsupport.states.ExecutionState
import net.corda.core.messaging.vaultQueryBy
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
}
