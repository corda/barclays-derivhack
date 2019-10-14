package com.derivhack.webserver.models.view

import net.corda.core.identity.Party
import org.isda.cdm.Confirmation

class ConfirmationViewModel(val linearId: String,
                            val party: List<Party>,
                            val confirmation: Confirmation)