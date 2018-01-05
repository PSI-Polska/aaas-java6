package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.*

/**
 * CalculationExecution implementation based on TimeSeries.
 * TimeSeries values are read from [TimeSeriesRepository] before the call and the results are saved back with repository after the call.
 * CalculationExecution uses [ScriptSynchronizer] to wait for synchronization to end in case synchronization is ongoing.
 */
class TimeSeriesBasedCalculationExecution(val synchronizer: ScriptSynchronizer,
                                          val tsRepository: TimeSeriesRepository,
                                          val engine: Engine) : CalculationExecution {

    override fun call(calcDef: CalculationDefinition) {
        if (synchronizer.isUnderSynchronization()) synchronizer.waitEnd()

        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(it.value, calcDef.begin, calcDef.end) }

        val mappedResult = engine.call(calcDef, inTs)

        mappedResult.map { symbolToTsId(calcDef, it) to it.second }
                .forEach { tsRepository.save(it.first, it.second) }
    }

    private fun symbolToTsId(calcDef: CalculationDefinition, it: Pair<Symbol, TS>) =
            calcDef.timeSeriesIdsOut[it.first] ?: throw CalculationException("""Unable to match result time series ${it.first} with definition ${calcDef.timeSeriesIdsOut.keys}.""")
}