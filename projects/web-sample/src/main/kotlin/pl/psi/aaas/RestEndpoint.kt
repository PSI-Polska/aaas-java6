package pl.psi.aaas

import pl.psi.aaas.usecase.timeseries.TSCalcDefWithValuesDTO
import pl.psi.aaas.usecase.timeseries.TimeSeriesCalculationDefinition
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/call")
class RestEndpoint @Inject constructor(private val facade: Facade<TimeSeriesCalculationDefinition>) {

    @GET
    fun callDef() {
        facade.callScript(getDefinition())
    }

    fun getDefinition(): TSCalcDefWithValuesDTO {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}