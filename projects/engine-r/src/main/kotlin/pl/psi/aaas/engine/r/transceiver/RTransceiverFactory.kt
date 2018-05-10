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
    fun <D : CalculationDefinition> get(param: Parameter<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, D> =
            when (param) {
                is Primitive -> primitiveTransceiver(param, conn)
                is Vector<*> -> vectorTransceiver(param, conn)
                is DataFrame -> DataFrameTransceiver<D>(conn) as RValuesTransceiver<Parameter<*>, *, D>
            }

    // TODO this should be removed when TSDataFrame is Parameter<??>
    fun <D : CalculationDefinition> get(conn: RConnection): RValuesTransceiver<TSDataFrame, TSDataFrame, D> {
        // TODO 05.05.2018 kskitek: handle different V and R types
        return TSValuesTransceiver(conn) as RValuesTransceiver<TSDataFrame, TSDataFrame, D>
    }

}

private fun <D : CalculationDefinition> vectorTransceiver(param: Vector<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, D> = //EngineValuesSender TODO
        when (param.elemClazz) {
            String::class.java    -> RNativeTransceiver<Vector<String>, Vector<String>, D>({ REXPString(it.value) }, conn)
                as RValuesTransceiver<Parameter<*>, *, D>
            Long::class.java      -> RNativeTransceiver<Vector<Long>, Vector<Long>, D>(
                    { REXPDouble(it.value.map { it?.toDouble() ?: REXPDouble.NA }.toDoubleArray()) }
                    , conn) as RValuesTransceiver<Parameter<*>, *, D>
            Double::class.java    -> RNativeTransceiver<Vector<Double>, Vector<Double>, D>(
                { REXPDouble(it.value.map { it ?: REXPDouble.NA }.toDoubleArray()) }
                , conn) as RValuesTransceiver<Parameter<*>, *, D>
    // TODO 09.05.2018 kskitek: this is MONSTER!!
            Boolean::class.java   -> RNativeTransceiver<Vector<Boolean>, Vector<Boolean>, D>(
                {
                    REXPLogical(it.value
                            .map {
                                when (it) {
                                    null -> REXPLogical.NA
                                    true -> REXPLogical.TRUE
                                    false -> REXPLogical.FALSE
                                }
                            }.toByteArray())
                }
                , conn)
        ZonedDateTime::class.java -> ArrayDateTimeTransceiver<D>(conn) as RValuesTransceiver<Parameter<*>, *, D>
        else -> throw CalculationException("Not implemented array parameter type ${param.elemClazz}")
    } as RValuesTransceiver<Parameter<*>, *, D>

private fun <D : CalculationDefinition> primitiveTransceiver(param: Primitive<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, D> =
        when (param.clazz) {
            String::class.java -> RNativeTransceiver<Parameter<String>, Parameter<String>, D>({ REXPString(it.value) }, conn)
            Long::class.java -> RNativeTransceiver<Parameter<Long>, Parameter<Long>, D>({ REXPDouble(it.value.toDouble()) }, conn)
            Double::class.java -> RNativeTransceiver<Parameter<Double>, Parameter<Double>, D>({ REXPDouble(it.value) }, conn)
            Boolean::class.java -> RNativeTransceiver<Parameter<Boolean>, Parameter<Boolean>, D>({ REXPLogical(it.value) }, conn)
            ZonedDateTime::class.java -> DateTimeTransceiver<D>(conn) as RValuesTransceiver<*, *, *>
            else -> throw CalculationException("Not implemented parameter type ${param.clazz}")
        } as RValuesTransceiver<Parameter<*>, *, D>
