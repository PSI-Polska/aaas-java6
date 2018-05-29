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
            df.value.map { Q(it.symbol, it.vector, RValuesTransceiverFactory.get(it.vector, session), it.vector.elemClazz as Class<Any>) }
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
        if (!result.isList)
            throw IllegalStateException("$name has to be data.frame")
        val dfDefinition = definition.outParameters[name]?.let { it as DataFrame } ?: throw Exception("")
        val transceivers = findAllTransceivers(dfDefinition)
        val dfFromR = result.asList()

        val columns = transceivers.map {
            dfFromR[it.symbol]
                    ?: throw IllegalStateException("Result DataFrame $name does not contain column ${it.symbol}.")
            val receivedValues = it.transceiver.receive(it.symbol, dfFromR[it.symbol], definition) as Array<Any?>
            Column(it.symbol, Parameter.ofArray(receivedValues, it.clazz))
        }.toTypedArray()

        return Parameter.ofDataFrame(columns, dfDefinition.columnClasses)
    }

    private data class Q(val symbol: Symbol,
                         val vector: Vector<*>,
                         val transceiver: RValuesTransceiver<Vector<*>, *, CalculationDefinition>,
                         val clazz: Class<Any>)
}
