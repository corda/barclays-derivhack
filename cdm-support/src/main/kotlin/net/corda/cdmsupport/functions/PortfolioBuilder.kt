package net.corda.cdmsupport.functions

import net.corda.cdmsupport.states.ExecutionState
import org.isda.cdm.Portfolio


fun portfolioBuilderFromExecutions(executions : List<ExecutionState>,
                                   portfolioInstructionsJson: String): Portfolio {


    //TODO Add your code for building CDM Portfolio Object here*

    return Portfolio.PortfolioBuilder().build()
}
