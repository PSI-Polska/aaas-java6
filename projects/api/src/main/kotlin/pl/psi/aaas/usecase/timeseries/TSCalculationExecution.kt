package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.Engine
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.CalculationExecution
import java.time.Duration


/**
 * CalculationExecution implementation based on TimeSeries.
 * TimeSeries values are read from [TimeSeriesRepository] before the call and the results are saved back with repository after the call.
 */
class TSCalculationExecution(val tsRepository: TSRepository,
                             val engine: Engine<TSCalcDefWithValues, TSDataFrame, TSDataFrame>)
    : CalculationExecution<TSCalculationDefinition, Unit> {
    // TODO 05.05.2018 kskitek: bring synchronizer back

    override fun call(calcDef: TSCalculationDefinition) {
        val resolution = Duration.ofHours(1)
        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(prepQuery(it.value, calcDef)) }
                .map { it.first to Triple(calcDef.begin, resolution, it.second) }.toMap()

        val resultDF = engine.call(TSCalcDefWithValues(calcDef, TSDataFrame(inTs)))

        val mappedResult = resultDF?.toMap() ?: emptyMap()
        mappedResult.map { symbolToOutTsId(calcDef, it) to it.value }
                .forEach { tsRepository.save(prepQuery(it.first, calcDef), it.second) }
    }

    private fun prepQuery(id: Long, calcDef: TSCalculationDefinition) =
            TSQuery(id, calcDef.begin, calcDef.end)

    private fun symbolToOutTsId(calcDef: TSCalculationDefinition, it: Map.Entry<Symbol, TS>) =
            calcDef.timeSeriesIdsOut[it.key]
                    ?: throw CalculationException("""Unable to match result time series ${it.key} with definition ${calcDef.timeSeriesIdsOut.keys}.""")
}

