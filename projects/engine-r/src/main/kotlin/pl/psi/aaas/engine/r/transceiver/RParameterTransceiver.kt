package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.DataFrame
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Vector
import java.time.ZonedDateTime

class RNativeTransceiver<in V : Parameter<*>, R, in D : CalculationDefinition>(
        private val outTransformer: (v: V) -> REXP, override val session: RConnection)
    : RValuesTransceiver<V, R, D> {

    override fun send(value: V, definition: D) {
        session.assign("v", outTransformer(value))
        session.voidEval("print(v)")
        session.voidEval("str(v)")
    }

    override fun receive(result: Any?, definition: D): R? {
        TODO("not implemented")
    }
}

//private fun stringTransformer(): (StringParam) -> REXP = { REXPString(it.value) }
//private fun longTransformer(): (LongParam) -> REXP = { REXPDouble(it.value.toDouble()) }
//
//class StringTransceiver<in D : CalculationDefinition>(override val session: RConnection)
//    : RNativeTransceiver<StringParam, StringParam, D>(StringTransceiver.outTranformer) {
//    companion object {
//        val outTranformer: (StringParam) -> REXP = { REXPString(it.value) }
//    }
//}
//
//class LongTransceiver<in D : CalculationDefinition>(override val session: RConnection)
//    : RNativeTransceiver<LongParam, LongParam, D>({ REXPDouble(it.value.toDouble()) })
//
//class DoubleTransceiver<in D : CalculationDefinition>(override val session: RConnection)
//    : RNativeTransceiver<DoubleParam, DoubleParam, D>({ REXPDouble(it.value) })
//
//class BooleanTransceiver<D : CalculationDefinition>(override val session: RConnection)
//    : RNativeTransceiver<BooleanParam, BooleanParam, D>({ REXPLogical(it.value) })
//
class DateTimeTransceiver<in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<Parameter<ZonedDateTime>, Parameter<ZonedDateTime>, D> {

    override fun send(value: Parameter<ZonedDateTime>, definition: D) {
        val epochSecond = value.value.toEpochSecond()
//        session.assign(value.name, REXPDouble(epochSecond.toDouble()))
//        session.voidEval("${value.name} <- structure(${value.name}, class=c('POSIXt','POSIXct'))")
//        attr(d1, "tzone") <- "UTC" TODO
//        session.voidEval(value.name)
        session.assign("dt", REXPDouble(epochSecond.toDouble()))
        session.voidEval("dt <- structure(dt, class=c('POSIXt','POSIXct'))")
        session.voidEval("print(dt)")
    }

    override fun receive(result: Any?, definition: D): Parameter<ZonedDateTime>? {
        TODO("not implemented")
    }
}

class ArrayDateTimeTransceiver<in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<Parameter<Array<ZonedDateTime>>, Parameter<Array<ZonedDateTime>>, D> {

    override fun send(value: Parameter<Array<ZonedDateTime>>, definition: D) {
        val epochSecond = value.value.map { it.toEpochSecond() }
                .map { it.toDouble() }.toDoubleArray()
//        session.assign(value.name, REXPDouble(epochSecond.toDouble()))
//        session.voidEval("${value.name} <- structure(${value.name}, class=c('POSIXt','POSIXct'))")
//        attr(d1, "tzone") <- "UTC" TODO
//        session.voidEval(value.name)
        session.assign("dt", REXPDouble(epochSecond))
        session.voidEval("dt <- structure(dt, class=c('POSIXt','POSIXct'))")
        session.voidEval("print(dt)")
        session.voidEval("str(dt)")
    }

    override fun receive(result: Any?, definition: D): Parameter<Array<ZonedDateTime>>? {
        TODO("not implemented")
    }
}

class ArrayTransceiver<T: Any?, in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<Parameter<Array<T>>, Parameter<Array<T>>, D> {

    override fun send(value: Parameter<Array<T>>, definition: D) {
//        session.assign("arrS", REX)
    }

    override fun receive(result: Any?, definition: D): Parameter<Array<T>>? {
        TODO("not implemented")
    }

}

class DataFrameTransceiver<in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<DataFrame, DataFrame, D> {

    private fun findAllTransceivers(df: DataFrame): Array<Pair<Vector<*>, RValuesTransceiver<Vector<*>, *, D>>> =
            df.value.map { it.second }
                    .map { it to RValuesTransceiverFactory.get<D>(it, session) }.toTypedArray()

    override fun send(value: DataFrame, definition: D) {
        val columns = value.value.map { it.first }.joinToString { """ "$it" """ }

        session.voidEval("df <- data.frame()")
        findAllTransceivers(value).forEach { it.second.send(it.first, definition) }
        session.voidEval("df <- cbind($columns)")
        session.voidEval("names(df) <- c($columns)")

        session.voidEval("str(df)")
    }

    override fun receive(result: Any?, definition: D): DataFrame? {
        TODO("not implemented")
    }

}