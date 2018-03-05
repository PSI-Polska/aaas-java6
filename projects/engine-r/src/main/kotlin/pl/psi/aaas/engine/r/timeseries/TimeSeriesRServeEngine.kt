package pl.psi.aaas.engine.r.timeseries

import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.slf4j.LoggerFactory
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.engine.r.timeseries.TimeSeriesRServeEngine.Companion.log
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.timeseries.MappedTS
import pl.psi.aaas.usecase.timeseries.Symbol
import pl.psi.aaas.usecase.timeseries.TimeSeriesCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TimeSeriesWithValuesCalculationDefinition

class TimeSeriesRServeEngine(private val connectionProvider: RConnectionProvider) : RServeEngine<MappedTS>(connectionProvider) {
    companion object {
        internal val log = LoggerFactory.getLogger(TimeSeriesRServeEngine::class.java)
    }

    override fun beforeExecute(conn: RConnection, calcDef: TimeSeriesWithValuesCalculationDefinition) {
        calcDef.tsValues.sendValues(conn)
    }

    override fun afterExecute(conn: RConnection, result: REXP, calcDef: TimeSeriesWithValuesCalculationDefinition): MappedTS {
        val rList = result.processResults(calcDef)
        rList.logRList()
        return rList.mapDataFrameToTS(calcDef)
    }

    private fun REXP.processResults(calcDef: CalculationDefinition): RList =
            when {
                isList -> asList()
                isNumeric -> RList()
                else -> {
                    log.warn("""Definition: $calcDef returned ${this.toDebugString()}""")
                    RList()
                }
            }
}

internal fun MappedTS.sendValues(conn: RConnection) {
    val allVectors = joinToString { it.first }
    log.debug("""Sending values $allVectors""")
    forEach { conn.assign(it.first, it.second) }
    conn.voidEval("""dfIn <- data.frame($allVectors)""")
}

internal fun RList.logRList() {
    val dfColumns = keys()?.joinToString() ?: ""
    log.debug("""Result DF columns: $dfColumns""")
}

internal fun RList.mapDataFrameToTS(calcDef: TimeSeriesCalculationDefinition): MappedTS {
    val (results, missingResults) = calcDef.partitionResults(this)

    if (missingResults.isNotEmpty()) {
        val missingSymbols = missingResults.joinToString { it.first }
        log.error("""Definition: ${calcDef.calculationScriptPath} did not return required symbols: $missingSymbols""")
        throw CalculationException("Script did not return expected results: " + missingResults)
    } else {
        return results.map { it.first to it.second.asDoubles() }
    }

}

internal fun TimeSeriesCalculationDefinition.partitionResults(dataFrame: RList): Pair<List<Pair<Symbol, REXP>>, List<Pair<Symbol, REXP?>>> {
    val (results, missingResults) = timeSeriesIdsOut
            .map { it.key to dataFrame[it.key] as REXP? }
            .partition { it.second != null }
    return Pair(results as List<Pair<Symbol, REXP>>, missingResults)
}
