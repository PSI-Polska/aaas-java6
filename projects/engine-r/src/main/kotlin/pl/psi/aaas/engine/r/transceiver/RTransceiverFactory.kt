package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXPString
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.engine.r.timeseries.TSValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.timeseries.TSDataFrame
import java.time.ZonedDateTime

// TODO 09.05.2018 kskitek: this factory has to be generic in terms of Engine impl; provided separately; registration of impls with SPI
object RValuesTransceiverFactory {
    fun <D : CalculationDefinition> get(param: Parameter<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, D> =
            if (param.primitive)
                when (param.clazz) {
                    String::class.java        -> RNativeTransceiver<Parameter<String>, Parameter<String>, D>({ REXPString(it.value) }, conn)
                    ZonedDateTime::class.java -> DateTimeTransceiver<D>(conn) as RValuesTransceiver<*, *, *>
                    else                      -> throw CalculationException("Not implemented parameter type ${param.clazz}")
                } as RValuesTransceiver<Parameter<*>, *, D>
            else
                when (param.elemClazz!!) {
                    String::class.java -> RNativeTransceiver<Parameter<Array<String>>, Parameter<Array<String>>, D>({ REXPString(it.value) }, conn) as RValuesTransceiver<Parameter<*>, *, D>
                    else               -> throw CalculationException("Not implemented array parameter type ${param.elemClazz}")
                } as RValuesTransceiver<Parameter<*>, *, D>
//    ArrayTransceiver<D>(conn) as RValuesTransceiver<Parameter<*>, *, D>


    // TODO this should be removed when TSDataFrame is Parameter<??>
    fun <D : CalculationDefinition> get(conn: RConnection): RValuesTransceiver<TSDataFrame, TSDataFrame, D> {
        // TODO 05.05.2018 kskitek: handle different V and R types
        return TSValuesTransceiver(conn) as RValuesTransceiver<TSDataFrame, TSDataFrame, D>
    }

}
