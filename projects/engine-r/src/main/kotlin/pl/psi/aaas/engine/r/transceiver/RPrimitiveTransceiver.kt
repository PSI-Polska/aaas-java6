package pl.psi.aaas.engine.r.transceiver

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPLogical
import org.rosuda.REngine.REXPString
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.Parameter

internal class RPrimitiveTransceiver<in V : Parameter<*>, R>(
        override val session: RConnection,
        private val outTransformer: (v: V) -> REXP,
        private val inTransformer: (r: REXP) -> R?)
    : RNativeTransceiver<V, R>(session, outTransformer, inTransformer) {

    companion object {
        fun string(session: RConnection) = RPrimitiveTransceiver<Parameter<String>, String>(
                session,
                { REXPString(it.value) },
                {
                    if (it.isString) it.asString()
                    else null
                })

        fun long(session: RConnection) = RPrimitiveTransceiver<Parameter<Long>, Long>(
                session,
                { REXPDouble(it.value.toDouble()) },
                {
                    if (it.isNumeric) it.asDouble().toLong()
                    else null
                })

        fun double(session: RConnection) = RPrimitiveTransceiver<Parameter<Double>, Double>(
                session,
                { REXPDouble(it.value) },
                {
                    if (it.isNumeric) it.asDouble()
                    else null
                })

        fun boolean(session: RConnection) = RPrimitiveTransceiver<Parameter<Boolean>, Boolean>(
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
        val result = if (result == null)
            session.get(name, null, true)
        else
            result as REXP
        return inTransformer(result)
    }
}

internal class DateTimeTransceiver(override val session: RConnection)
    : RValuesTransceiver<Parameter<DateTime>, DateTime, CalculationDefinition> {

    override fun send(name: String, value: Parameter<DateTime>, definition: CalculationDefinition) {
        val epochSecond = value.value.millis / 1000
        session.assign(name, REXPDouble(epochSecond.toDouble()))
        session.voidEval("$name <- structure($name, class=c('POSIXt','POSIXct'))")
        session.voidEval("""attr($name, "tzone") <- "UTC"""")
    }

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): DateTime? {
        val result = session.get(name, null, true)
        return with(result.asDouble().toLong()) {
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneOffset.UTC)
        }
    }
}
