package pl.psi.aaas.engine.r.timeseries

import org.rosuda.REngine.REXP
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.slf4j.LoggerFactory
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.Column
import pl.psi.aaas.usecase.timeseries.MappedTS
import pl.psi.aaas.usecase.timeseries.Symbol
import pl.psi.aaas.usecase.timeseries.TSCalculationDefinition

class TSValuesTransceiver(override val session: RConnection) : RValuesTransceiver<MappedTS, MappedTS, TSCalculationDefinition> {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
    }

    override fun send(values: MappedTS, definition: TSCalculationDefinition) {
        val vectorNames = values.keys
        val vectorCSV = vectorNames.joinToString()
        log.debug("Sending values $vectorCSV")

        values.allButDT().forEach {
            val values = it.value.map { it.second ?: REXPDouble.NA }.toDoubleArray()
            session.assign(it.key, values)
        }
        session.voidEval("""dfIn <- data.frame($vectorCSV)""")
    }

    override fun receive(result: Any?, definition: TSCalculationDefinition): MappedTS? =
            when (result) {
                null     -> RList().toMappedTS(definition)
                is RList -> result.toMappedTS(definition)
                else     -> throw CalculationException("${result.javaClass} is not type of RList.")
            }
}

private fun RList.toMappedTS(def: TSCalculationDefinition): MappedTS? {
    val (results, missingResults) = def.partitionResults(this)

    if (missingResults.isNotEmpty()) {
        val missingSymbols = missingResults.joinToString { it.first }
        throw CalculationException("Script did not return expected results: $missingSymbols")
    } else {
        results.map{
            it.first to it.second.asDoubles()
        }
        return MappedTS(names.toTypedArray(), values.toTypedArray())
        // TODO 07.05.2018 kskitek: this will not allow to handle heterogeneous DataFrames
//        return results.map { it.first to it.second.asDoubles() }
    }
}


private fun TSCalculationDefinition.partitionResults(dataFrame: RList): Pair<Array<Pair<Symbol, REXP>>, Array<Pair<Symbol, REXP?>>> {
    val (results, missingResults) = timeSeriesIdsOut
            .map { it.key to dataFrame[it.key] as REXP? }
            .partition { it.second != null }
    return Pair(results.toTypedArray() as Array<Pair<Symbol, REXP>>, missingResults.toTypedArray())
}
