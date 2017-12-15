package pl.psi.aaas.usecase

import java.time.ZonedDateTime

typealias Symbol = String
typealias Parameters = Map<String, String>

/**
 * DTO to move calculation definition information.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property calculationScriptPath not empty path to calculation R script
 */
data class CalculationDefinition(val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                                 val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                                 val begin: ZonedDateTime,
                                 val end: ZonedDateTime,
                                 val calculationScriptPath: String,
                                 val additionalParameters: Parameters = emptyMap())

interface ScriptExecution {
    fun call(calcDef: CalculationDefinition)
}

/**
 * Calculation Exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
class CalculationException(override val message: String, override val cause: Throwable? = null) : RuntimeException(message)

// TODO 13.12.2017 kskitek: how to name this class?!
class JustScriptExecution(val synchronizer: ScriptSynchronizer,
                                   val tsRepository: TimeSeriesRepository,
                                   val engine: Engine) : ScriptExecution {

    override fun call(calcDef: CalculationDefinition) {
        synchronizer.isUnderSynchronization()

        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(it.value, calcDef.begin, calcDef.end) }

        val mappedResult = engine.schedule(calcDef, inTs)

        mappedResult.map { symbolToTsId(calcDef, it) to it.second }
                .forEach { tsRepository.save(it.first, it.second) }
    }

    private fun symbolToTsId(calcDef: CalculationDefinition, it: Pair<Symbol, TS>) =
            // TODO 12.12.2017 kskitek: throw error when null!!!
            calcDef.timeSeriesIdsOut[it.first]!!
}