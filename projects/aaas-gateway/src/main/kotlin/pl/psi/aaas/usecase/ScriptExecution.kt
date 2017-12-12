package pl.psi.aaas.usecase

import java.util.concurrent.CompletableFuture

/**
 * DTO to move calculation definition information. Required by [CalculationGateway].
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property calculationScriptPath not empty path to calculation R script
 */
// TODO 01.08.2017 kskitek: change TsIdIn and Out to TSDef from TS-API package
data class CalculationDefinition(val timeSeriesIdsIn: Map<String, Long>,
                                 val timeSeriesIdsOut: Map<String, Long>,
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

        engine.schedule(calcDef, inTs)
    }
}
