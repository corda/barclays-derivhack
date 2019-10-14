import com.rosetta.model.lib.records.Date
import org.isda.cdm.Party
import org.isda.cdm.Security

data class PortfolioInstructions(val date : Date,
                                 val party : Party,
                                 val security : Security)