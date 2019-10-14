package com.derivhack.webserver.models.view

import net.corda.core.identity.Party
import org.isda.cdm.Execution

class ExecutionViewModel(val linearId: String,
                         val parties: List<Party>,
                         val execution: Execution,
                         val eventRef: String,
                         val status: String)
