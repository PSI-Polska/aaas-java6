package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Symbol
import pl.psi.aaas.usecase.parameters.Column
import pl.psi.aaas.usecase.parameters.DataFrame
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Vector

internal class DataFrameTransceiver(override val session: RConnection)
    : RValuesTransceiver<DataFrame, DataFrame, CalculationDefinition> {

    private fun findAllTransceivers(df: DataFrame): Array<Q> =
            df.value.map { it.symbol to it.vector }
                    .map { Q(it.first, it.second, RValuesTransceiverFactory.get(it.second, session), it.second.elemClazz as Class<Any>) }
                    .toTypedArray()

    override fun send(name: String, value: DataFrame, definition: CalculationDefinition) {
        val columnNamesCSV = value.value.map { it.symbol }.joinToString { """ "$it" """ }

        val randColNames = generateNames(name, value.value.size)
        val randColNamesCSV = randColNames.joinToString()

        findAllTransceivers(value)
                .map { it.vector to it.transceiver }
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
        val result = session.get(name, null, true)
        val df = definition.outParameters[name]?.let { it as DataFrame } ?: throw Exception("")
        val transceivers = findAllTransceivers(df)
        val rDFasList = result.asList()

        val columns = transceivers.map {
            val transformedValues = it.transceiver.receive(it.symbol, rDFasList[it.symbol], definition) as Array<Any?>
            val vector = Parameter.ofArray(transformedValues, it.clazz)
            Column(it.symbol, vector)
        }.toTypedArray()

        return Parameter.ofDataFrame(columns, df.columnClasses)
    }

    private data class Q(val symbol: Symbol,
                         val vector: Vector<*>,
                         val transceiver: RValuesTransceiver<Vector<*>, *, CalculationDefinition>,
                         val clazz: Class<Any>)
}
