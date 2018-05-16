package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.Parameter

internal open class RNativeTransceiver<in V : Parameter<*>, R>(
        override val session: RConnection,
        private val outTransformer: (v: V) -> REXP,
        private val inTransformer: (r: REXP) -> R? = { null })
    : RValuesTransceiver<V, R, CalculationDefinition> {

    override fun send(name: String, value: V, definition: CalculationDefinition) =
            session.assign(name, outTransformer(value))

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): R? =
            if (result != null && result is REXP)
                inTransformer(result)
            else {
                val result = session.get(name, null, true)
                if (result.isNull)
                    null
                else
                    inTransformer(result)
            }
}