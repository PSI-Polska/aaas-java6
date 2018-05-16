package pl.psi.aaas.usecase.timeseries

import org.joda.time.Duration
import pl.psi.aaas.Engine
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.CalculationExecution
import pl.psi.aaas.usecase.Parameters
import pl.psi.aaas.usecase.Symbol
import pl.psi.aaas.usecase.parameters.DataFrame


/**
 * CalculationExecution implementation based on TimeSeries.
 * TimeSeries values are read from [TimeSeriesRepository] before the call and the results are saved back with repository after the call.
 */
@Deprecated("This class is destined for removal in 0.3. All parameters will be sent with the help of Parameter class without need to implement default use cases in the library.", replaceWith = ReplaceWith("In 0.3 There will be no CalculationExecution class."))
class TSCalculationExecution(val tsRepository: TSRepository,
                             val engine: Engine<TSCalcDefWithValues, TSDataFrame, Parameters>)
    : CalculationExecution<TSCalculationDefinition, Unit> {

    override fun call(calcDef: TSCalculationDefinition) {
        val resolution = Duration.standardHours(1)
        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(prepQuery(it.value, calcDef)) }
                .map { it.first to Triple(calcDef.begin, resolution, it.second) }.toMap()

        val result = engine.call(TSCalcDefWithValues(calcDef, TSDataFrame(inTs))) ?: emptyMap()

        tsRepository.save(prepQuery(1, calcDef), result.get("dfOut") as DataFrame);

//        return resultDF?.

//        val mappedResult = resultDF?.toMap() ?: emptyMap()
//        mappedResult.map { symbolToOutTsId(calcDef, it) to it.value }
//                .forEach { tsRepository.save(prepQuery(it.first, calcDef), it.second) }
    }

    private fun prepQuery(id: Long, calcDef: TSCalculationDefinition) =
            TSQuery(id, calcDef.begin, calcDef.end)

    private fun symbolToOutTsId(calcDef: TSCalculationDefinition, it: Map.Entry<Symbol, TS>) =
            calcDef.timeSeriesIdsOut[it.key]
                    ?: throw CalculationException("""Unable to match result time series ${it.key} with definition ${calcDef.timeSeriesIdsOut.keys}.""")
}

