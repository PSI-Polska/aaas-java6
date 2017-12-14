package pl.psi.aaas

import pl.psi.aaas.engine.REngineConfiguration
import pl.psi.aaas.engine.RServeEngine
import pl.psi.aaas.usecase.*
import javax.inject.Inject


interface Facade {
    fun callScript(calcDef: CalculationDefinition)
}


/**
 * Calculation endpoint.
 */
class CdiFacade : Facade {
    @Inject
    lateinit var exec: ScriptExecution

    override fun callScript(calcDef: CalculationDefinition) {
        exec.call(calcDef)
    }
}

object FixedFacade : Facade {
    val engine: Engine = RServeEngine(REngineConfiguration("192.168.99.100", 6311))
    val synchronizer: ScriptSynchronizer = NoSynchronizationSynchronizer()
    val tsRepository: TimeSeriesRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: CalculationDefinition) {
        val scriptExecution = JustScriptExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}
