package com.derivhack.webserver.models.view

import net.corda.core.identity.Party
import org.isda.cdm.Affirmation

class AffirmationViewModel(val linearId: String,
                           val party: List<Party>,
                           val affirmation: Affirmation)