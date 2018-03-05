package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.CalculationExecution
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.ScriptSynchronizer

/**
 * CalculationExecution implementation based on TimeSeries.
 * TimeSeries values are read from [TimeSeriesRepository] before the call and the results are saved back with repository after the call.
 * CalculationExecution uses [ScriptSynchronizer] to wait for synchronization to end in case synchronization is ongoing.
 */
class TimeSeriesBasedCalculationExecution(val synchronizer: ScriptSynchronizer,
                                          val tsRepository: TimeSeriesRepository,
                                          val engine: Engine<TimeSeriesWithValuesCalculationDefinition, MappedTS>)
    : CalculationExecution<TimeSeriesCalculationDefinition> {

    override fun call(calcDef: TimeSeriesCalculationDefinition) {
        if (synchronizer.isUnderSynchronization()) synchronizer.waitEnd()

        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(it.value, calcDef.begin, calcDef.end) }

        val mappedResult = engine.call(TSCalcDefWithValuesDTO(calcDef, inTs))

        mappedResult.map { symbolToTsId(calcDef, it) to it.second }
                .forEach { tsRepository.save(it.first, calcDef.begin, it.second) }
    }

    private fun symbolToTsId(calcDef: TimeSeriesCalculationDefinition, it: Pair<Symbol, TS>) =
            calcDef.timeSeriesIdsOut[it.first] ?: throw CalculationException("""Unable to match result time series ${it.first} with definition ${calcDef.timeSeriesIdsOut.keys}.""")
}