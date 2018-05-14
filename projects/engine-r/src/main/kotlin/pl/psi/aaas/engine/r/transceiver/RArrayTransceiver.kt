package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPLogical
import org.rosuda.REngine.REXPString
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.Parameter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

// TODO try to generify RArrayTransceiver to reduce code duplication in Array and Primitive Transceiver
internal class RArrayTransceiver<in V : Parameter<Array<*>>, R>(
        override val session: RConnection,
        private val outTransformer: (v: V) -> REXP,
        private val inTransformer: (r: REXP) -> R?)
    : RNativeTransceiver<V, R>(session, outTransformer, inTransformer) {

    companion object {
        fun string(session: RConnection) = RPrimitiveTransceiver<Parameter<Array<String?>>, Array<String?>>(
                session,
                { REXPString(it.value) },
                {
                    if (it.isString) it.asStrings()
                    else emptyArray()
                })

        fun long(session: RConnection) = RPrimitiveTransceiver<Parameter<Array<Long?>>, Array<Long>>(
                session,
                { REXPDouble(it.value.map { it?.toDouble() ?: REXPDouble.NA }.toDoubleArray()) },
                {
                    if (it.isNumeric) it.asDoubles().map { it.toLong() }.toTypedArray()
                    else emptyArray()
                })

        fun double(session: RConnection) = RPrimitiveTransceiver<Parameter<Array<Double?>>, Array<Double?>>(
                session,
                { REXPDouble(it.value.map { it ?: REXPDouble.NA }.toDoubleArray()) },
                {
                    if (it.isNumeric) it.asDoubles().toTypedArray() as Array<Double?>?
                    else emptyArray()
                })

        fun boolean(session: RConnection) = RPrimitiveTransceiver<Parameter<Array<Boolean?>>, Array<Boolean?>>(
                session,
                {
                    REXPLogical(it.value.map {
                        when (it) {
                            null -> REXPLogical.NA
                            true -> REXPLogical.TRUE
                            false -> REXPLogical.FALSE
                        }
                    }.toByteArray())
                },
                {
                    if (it.isLogical)
                        it.asBytes().map {
                            when (it) {
                                REXPLogical.TRUE -> true
                                REXPLogical.FALSE -> false
                                REXPLogical.NA -> null
                                else -> throw IllegalStateException("Value ${it} is not of type Boolean")
                            }
                        }.toTypedArray()
                    else emptyArray()
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

class ArrayDateTimeTransceiver(override val session: RConnection)
    : RValuesTransceiver<Parameter<Array<ZonedDateTime>>, Array<ZonedDateTime?>, CalculationDefinition> {

    override fun send(name: String, value: Parameter<Array<ZonedDateTime>>, definition: CalculationDefinition) {
        val epochSecond = value.value.map { it.toEpochSecond() }
                .map { it.toDouble() }.toDoubleArray()

        session.assign(name, REXPDouble(epochSecond))
        session.voidEval("$name <- structure($name, class=c('POSIXt','POSIXct'))")
        session.voidEval("""attr($name, "tzone") <- "UTC"""")
    }

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): Array<ZonedDateTime?>? {
        val result = session.get(name, null, true)
        return if (result.isNull)
            null
        else with(result.asDoubles()) {
            this.map { it?.toLong() ?: 0L }
                    .map { Instant.ofEpochSecond(it) }
                    .map { ZonedDateTime.ofInstant(it, ZoneOffset.UTC) }.toTypedArray()
        }
    }
}

