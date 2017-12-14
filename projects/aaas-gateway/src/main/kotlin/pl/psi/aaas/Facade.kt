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
