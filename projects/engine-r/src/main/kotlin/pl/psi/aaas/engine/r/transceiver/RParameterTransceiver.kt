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

    override fun send(name: String, value: V, definition: D) =
            session.assign(name, outTransformer(value))

    override fun receive(name: String, result: Any?, definition: D): R? {
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

    override fun send(name: String, value: Parameter<ZonedDateTime>, definition: D) {
        val epochSecond = value.value.toEpochSecond()
        session.assign(name, REXPDouble(epochSecond.toDouble()))
        session.voidEval("$name <- structure($name, class=c('POSIXt','POSIXct'))")
        session.voidEval("""attr($name, "tzone") <- "UTC"""")
    }

    override fun receive(name: String, result: Any?, definition: D): Parameter<ZonedDateTime>? {
        TODO("not implemented")
    }
}

class ArrayDateTimeTransceiver<in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<Parameter<Array<ZonedDateTime>>, Parameter<Array<ZonedDateTime>>, D> {

    override fun send(name: String, value: Parameter<Array<ZonedDateTime>>, definition: D) {
        val epochSecond = value.value.map { it.toEpochSecond() }
                .map { it.toDouble() }.toDoubleArray()

        session.assign(name, REXPDouble(epochSecond))
        session.voidEval("$name <- structure($name, class=c('POSIXt','POSIXct'))")
        session.voidEval("""attr($name, "tzone") <- "UTC"""")
    }

    override fun receive(name: String, result: Any?, definition: D): Parameter<Array<ZonedDateTime>>? {
        TODO("not implemented")
    }
}


class DataFrameTransceiver<in D : CalculationDefinition>(override val session: RConnection)
    : RValuesTransceiver<DataFrame, DataFrame, D> {

    private fun findAllTransceivers(df: DataFrame): Array<Pair<Vector<*>, RValuesTransceiver<Vector<*>, *, D>>> =
            df.value.map { it.vector }
                    .map { it to RValuesTransceiverFactory.get<D>(it, session) }.toTypedArray()

    override fun send(name: String, value: DataFrame, definition: D) {
        val columnNamesCSV = value.value.map { it.symbol }.joinToString { """ "$it" """ }

        val randColNames = generateNames(name, value.value.size)
        val randColNamesCSV = randColNames.joinToString()

        findAllTransceivers(value)
                .zip(randColNames)
                .forEach {
                    val (param, randName) = it
                    val (columnValue, transceiver) = param
                    transceiver.send(randName, columnValue, definition)
                }
        session.voidEval("$name <- data.frame($randColNamesCSV)")
        session.voidEval("names($name) <- c($columnNamesCSV)")
    }

    private fun generateNames(name: String, size: Int) =
            (0 until size).map { "${name}Col$it" }

    override fun receive(name: String, result: Any?, definition: D): DataFrame? {
        TODO("not implemented")
    }

}