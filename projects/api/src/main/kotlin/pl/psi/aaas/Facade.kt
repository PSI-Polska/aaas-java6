package pl.psi.aaas

import pl.psi.aaas.usecase.CalculationDefinition

/**
 *
 */
interface Facade<in T : CalculationDefinition> {
    fun callScript(calcDef: T)
}

// TODO CDI Facade? Weld facade?