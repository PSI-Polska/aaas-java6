package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPLogical
import org.rosuda.REngine.REXPString
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.engine.r.timeseries.TSValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.parameters.DataFrame
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Primitive
import pl.psi.aaas.usecase.parameters.Vector
import pl.psi.aaas.usecase.timeseries.TSDataFrame
import java.time.ZonedDateTime

// TODO 09.05.2018 kskitek: this factory has to be generic in terms ofPrimitive Engine impl; provided separately; registration ofPrimitive impls with SPI
object RValuesTransceiverFactory {
    fun get(param: Parameter<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, CalculationDefinition> =
            when (param) {
                is Primitive -> primitiveTransceiver(param, conn)
                is Vector<*> -> vectorTransceiver(param, conn)
                is DataFrame -> DataFrameTransceiver(conn) as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
            }

    // TODO this should be removed when TSDataFrame is Parameter<??>
    fun get(conn: RConnection): RValuesTransceiver<TSDataFrame, TSDataFrame, CalculationDefinition> {
        // TODO 05.05.2018 kskitek: handle different V and R types
        return TSValuesTransceiver(conn) as RValuesTransceiver<TSDataFrame, TSDataFrame, CalculationDefinition>
    }

    private fun primitiveTransceiver(param: Primitive<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, CalculationDefinition> =
            when (param.clazz) {
                String::class.java -> RPrimitiveTransceiverFactory.string(conn)
                java.lang.Long::class.java -> RPrimitiveTransceiverFactory.long(conn)
                Long::class.java -> RPrimitiveTransceiverFactory.long(conn)
                java.lang.Double::class.java -> RPrimitiveTransceiverFactory.double(conn)
                Double::class.java -> RPrimitiveTransceiverFactory.double(conn)
                java.lang.Boolean::class.java -> RPrimitiveTransceiverFactory.boolean(conn)
                Boolean::class.java -> RPrimitiveTransceiverFactory.boolean(conn)
                ZonedDateTime::class.java -> DateTimeTransceiver(conn) as RValuesTransceiver<*, *, *>
                else -> throw CalculationException("Not implemented parameter type ${param.clazz}")
            } as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
}

private fun vectorTransceiver(param: Vector<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, CalculationDefinition> = //EngineValuesSender TODO
        when (param.elemClazz) {
            String::class.java -> RNativeTransceiver<Vector<String>, Vector<String>>(conn, { REXPString(it.value) })
                    as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
            Long::class.java -> RNativeTransceiver<Vector<Long>, Vector<Long>>(
                    conn, {
                REXPDouble(it.value.map {
                    it?.toDouble() ?: REXPDouble.NA
                }.toDoubleArray())
            }) as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
            Double::class.java -> RNativeTransceiver<Vector<Double>, Vector<Double>>(
                    conn, {
                REXPDouble(it.value.map {
                    it ?: REXPDouble.NA
                }.toDoubleArray())
            }) as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
        // TODO 09.05.2018 kskitek: this is MONSTER!!
            Boolean::class.java -> RNativeTransceiver<Vector<Boolean>, Vector<Boolean>>(
                    conn, {
                REXPLogical(it.value
                        .map {
                            when (it) {
                                null -> REXPLogical.NA
                                true -> REXPLogical.TRUE
                                false -> REXPLogical.FALSE
                            }
                        }.toByteArray())
            })
            java.lang.Boolean::class.java -> RNativeTransceiver<Vector<Boolean>, Vector<Boolean>>(
                    conn, {
                REXPLogical(it.value
                        .map {
                            when (it) {
                                null -> REXPLogical.NA
                                true -> REXPLogical.TRUE
                                false -> REXPLogical.FALSE
                            }
                        }.toByteArray())
            })
            ZonedDateTime::class.java -> ArrayDateTimeTransceiver(conn) as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>
            else -> throw CalculationException("Not implemented array parameter type ${param.elemClazz}")
        } as RValuesTransceiver<Parameter<*>, *, CalculationDefinition>

