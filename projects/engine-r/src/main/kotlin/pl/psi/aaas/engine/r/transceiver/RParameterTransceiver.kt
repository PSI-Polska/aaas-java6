package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.DataFrame
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Vector

open class RNativeTransceiver<in V : Parameter<*>, R>(
        override val session: RConnection,
        private val outTransformer: (v: V) -> REXP,
        private val inTransformer: (r: REXP) -> R? = { null })
    : RValuesTransceiver<V, R, CalculationDefinition> {

    override fun send(name: String, value: V, definition: CalculationDefinition) =
            session.assign(name, outTransformer(value))

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): R? {
        val result = session.get(name, null, true)
        return if (result.isNull)
            null
        else
            inTransformer(result)
    }
}

class DataFrameTransceiver(override val session: RConnection)
    : RValuesTransceiver<DataFrame, DataFrame, CalculationDefinition> {

    private fun findAllTransceivers(df: DataFrame): Array<Pair<Vector<*>, RValuesTransceiver<Vector<*>, *, CalculationDefinition>>> =
            df.value.map { it.vector }
                    .map { it to RValuesTransceiverFactory.get(it, session) }.toTypedArray()

    override fun send(name: String, value: DataFrame, definition: CalculationDefinition) {
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

    override fun receive(name: String, result: Any?, definition: CalculationDefinition): DataFrame? {
        TODO("not implemented")
    }

}