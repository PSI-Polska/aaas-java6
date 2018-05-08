package pl.psi.aaas.engine.r

import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.EngineValuesTranceiver
import pl.psi.aaas.usecase.CalculationDefinition

/**
 * TODO
 */
interface RValuesTransceiver<in V, out R, in D : CalculationDefinition> : EngineValuesTranceiver<V, R, D, RConnection>
