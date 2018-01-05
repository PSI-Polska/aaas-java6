package pl.psi.aaas

import pl.psi.aaas.usecase.CalculationDefinition

/**
 *
 */
interface Facade {
    fun callScript(calcDef: CalculationDefinition)
}

// TODO CDI Facade? Weld facade?