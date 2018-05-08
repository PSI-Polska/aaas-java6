package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.Engine
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.CalculationExecution


/**
 * CalculationExecution implementation based on TimeSeries.
 * TimeSeries values are read from [TimeSeriesRepository] before the call and the results are saved back with repository after the call.
 */
class TSCalculationExecution(val tsRepository: TSRepository,
                             val engine: Engine<TSCalcDefWithValues, TSDataFrame, TSDataFrame>)
    : CalculationExecution<TSCalculationDefinition, Unit> {
    // TODO 05.05.2018 kskitek: bring synchronizer back

    override fun call(calcDef: TSCalculationDefinition) {
        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(prepQuery(it.value, calcDef)) }.toMap()

        val resultDF = engine.call(TSCalcDefWithValues(calcDef, TSDataFrame(inTs)))

        val mappedResult = resultDF?.toMappedTS() ?: emptyMap()
        mappedResult.map { symbolToTsId(calcDef, it) to it.value }
                .forEach { tsRepository.save(prepQuery(it.first, calcDef), it.second) }
    }

    private fun prepQuery(id: Long, calcDef: TSCalculationDefinition) =
            TSQuery(id, calcDef.begin, calcDef.end)

    private fun symbolToTsId(calcDef: TSCalculationDefinition, it: Map.Entry<Symbol, TS>) =
            calcDef.timeSeriesIdsOut[it.key]
                    ?: throw CalculationException("""Unable to match result time series ${it.key} with definition ${calcDef.timeSeriesIdsOut.keys}.""")
}
