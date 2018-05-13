package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPLogical
import org.rosuda.REngine.REXPString
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.Parameter
import java.time.ZonedDateTime

internal class RPrimitiveTransceiverFactory<in V : Parameter<*>, R>(
        override val session: RConnection,
        private val outTransformer: (v: V) -> REXP,
        private val inTransformer: (r: REXP) -> R?)
    : RNativeTransceiver<V, R>(session, outTransformer, inTransformer) {

    companion object {
        fun string(session: RConnection) = RPrimitiveTransceiverFactory<Parameter<String>, String>(
                session,
                { REXPString(it.value) },
                {
                    if (it.isString) it.asString()
                    else null
                })

        fun long(session: RConnection) = RPrimitiveTransceiverFactory<Parameter<Long>, Long>(
                session,
                { REXPDouble(it.value.toDouble()) },
                {
                    if (it.isNumeric) it.asDouble().toLong()
                    else null
                })

        fun double(session: RConnection) = RPrimitiveTransceiverFactory<Parameter<Double>, Double>(
                session,
                { REXPDouble(it.value) },
                {
                    if (it.isNumeric) it.asDouble()
                    else null
                })

        fun boolean(session: RConnection) = RPrimitiveTransceiverFactory<Parameter<Boolean>, Boolean>(
                session,
                { REXPLogical(it.value) },
                {
                    if (it.isLogical) when (it.asInteger().toByte()) {
                        REXPLogical.TRUE -> true
                        REXPLogical.FALSE -> false
                        REXPLogical.NA -> null
                        else -> throw IllegalStateException("Value ${it.asBytes()[0]} is not of type Boolean")
                    }
                    else null
                })
    }

    override fun send(name: String, value: V, definition: CalculationDefinition) =
            session.assign(name, outTransformer(value))

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): R? {
        val result = session.get(name, null, true)
        return if (result.isNull)
            null
        else {
            inTransformer(result)
        }
    }
}

internal class DateTimeTransceiver(override val session: RConnection)
    : RValuesTransceiver<Parameter<ZonedDateTime>, Parameter<ZonedDateTime>, CalculationDefinition> {

    override fun send(name: String, value: Parameter<ZonedDateTime>, definition: CalculationDefinition) {
        val epochSecond = value.value.toEpochSecond()
        session.assign(name, REXPDouble(epochSecond.toDouble()))
        session.voidEval("$name <- structure($name, class=c('POSIXt','POSIXct'))")
        session.voidEval("""attr($name, "tzone") <- "UTC"""")
    }

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): Parameter<ZonedDateTime>? {
        TODO("not implemented")
    }
}
