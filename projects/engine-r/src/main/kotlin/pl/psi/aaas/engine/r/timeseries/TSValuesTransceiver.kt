package pl.psi.aaas.engine.r.timeseries

import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPGenericVector
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.slf4j.LoggerFactory
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.Column
import pl.psi.aaas.usecase.Symbol
import pl.psi.aaas.usecase.timeseries.TSCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TSDataFrame
import pl.psi.aaas.usecase.timeseries.toDoubleArray

class TSValuesTransceiver(override val session: RConnection) : RValuesTransceiver<TSDataFrame, TSDataFrame, TSCalculationDefinition> {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
    }

    override fun send(values: TSDataFrame, definition: TSCalculationDefinition) {
        val vectorNames = values.getColumns()
        val vectorCSV = vectorNames.joinToString()
        log.debug("Sending values $vectorCSV")

        values.getColumns()
                .forEach {
                    val doubleArray = values[it]?.toDoubleArray(REXPDouble.NA) ?: DoubleArray(0)
                    session.assign(it, doubleArray)
                }
        session.voidEval("""dfIn <- data.frame($vectorCSV)""")
    }

    override fun receive(result: Any?, definition: TSCalculationDefinition): TSDataFrame? =
            when (result) {
                null -> RList().toTSDataFrame(definition)
                is RList -> result.toTSDataFrame(definition)
                is REXPGenericVector -> receive(result.asList(), definition)
                else -> throw CalculationException("${result.javaClass} is not type of RList.")
            }
}

private fun RList.toTSDataFrame(def: TSCalculationDefinition): TSDataFrame? {
    val (results, missingResults) = def.partitionResults(this)

    if (missingResults.isNotEmpty()) {
        val missingSymbols = missingResults.joinToString { it.first }
        throw CalculationException("Script did not return expected results: $missingSymbols")
    } else {
        val names = ArrayList<String>(results.size)
        val values = ArrayList<Column<Double?>>(results.size)

        results.forEach {
            names.add(it.first)
            values.add(it.second.asDoubles().toTypedArray() as Column<Double?>)
        }
        return TSDataFrame(names.toTypedArray(), values.toTypedArray())
        // TODO 07.05.2018 kskitek: this will not allow to handle heterogeneous DataFrames
    }
}


private fun TSCalculationDefinition.partitionResults(dataFrame: RList): Pair<Array<Pair<Symbol, REXP>>, Array<Pair<Symbol, REXP?>>> {
    val (results, missingResults) = timeSeriesIdsOut
            .map { it.key to dataFrame[it.key] as REXP? }
            .partition { it.second != null }
    return Pair(results.toTypedArray() as Array<Pair<Symbol, REXP>>, missingResults.toTypedArray())
}
