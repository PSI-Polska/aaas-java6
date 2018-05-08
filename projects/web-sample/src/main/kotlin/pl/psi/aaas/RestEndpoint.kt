package pl.psi.aaas

import pl.psi.aaas.usecase.timeseries.TSCalcDef
import pl.psi.aaas.usecase.timeseries.TSCalculationDefinition
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/call")
class RestEndpoint @Inject constructor(private val facade: Facade<TSCalculationDefinition>) {

    @GET
    fun callDef() {
        facade.callScript(getDefinition())
    }

    fun getDefinition(): TSCalcDef =
            TSCalcDef(timeSeriesIdsIn = mapOf("A" to 1L, "B" to 2L),
                    timeSeriesIdsOut = mapOf("C" to 3L),
                    begin = ZonedDateTime.now(),
                    end = ZonedDateTime.now().plusDays(1),
                    calculationScript = "add",
                    additionalParameters = emptyMap())
}