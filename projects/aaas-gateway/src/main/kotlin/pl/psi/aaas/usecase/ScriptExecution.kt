package pl.psi.aaas.usecase

import java.util.concurrent.CompletableFuture

typealias Symbol = String

/**
 * DTO to move calculation definition information. Required by [CalculationGateway].
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property calculationScriptPath not empty path to calculation R script
 */
// TODO 01.08.2017 kskitek: change TsIdIn and Out to TSDef from TS-API package
data class CalculationDefinition(val timeSeriesIdsIn: Map<Symbol, Long>,
                                 val timeSeriesIdsOut: Map<Symbol, Long>,
                                 val calculationScriptPath: String)

typealias CalculationResult = CompletableFuture<Unit>

interface ScriptExecution {
    fun call(calcDef: CalculationDefinition)
}

internal class JustScriptExecution(val synchronizer: ScriptSynchronizer,
                                   val tsRepository: TimeSeriesRepository,
                                   val engine: Engine) : ScriptExecution {

    override fun call(calcDef: CalculationDefinition) {
        synchronizer.isUnderSynchronization()

        val inTs = calcDef.timeSeriesIdsIn.map { it.key to tsRepository.read(it.value) }

        val mappedResult = engine.schedule(calcDef, inTs)

        mappedResult.map { symbolToTsId(calcDef, it) to it.second }
                .forEach { tsRepository.save(it.first, it.second) }
    }

    private fun symbolToTsId(calcDef: CalculationDefinition, it: Pair<Symbol, TS>) =
            // TODO 12.12.2017 kskitek: throw error when null!!!
            calcDef.timeSeriesIdsOut.get(it.first)!!
}
