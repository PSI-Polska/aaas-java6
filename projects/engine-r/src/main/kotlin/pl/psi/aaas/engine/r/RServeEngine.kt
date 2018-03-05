package pl.psi.aaas.engine.r

import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.slf4j.LoggerFactory
import pl.psi.aaas.engine.r.RServeEngine.Companion.baseUserScriptPath
import pl.psi.aaas.engine.r.RServeEngine.Companion.log
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.timeseries.MappedTS
import pl.psi.aaas.usecase.timeseries.Symbol
import pl.psi.aaas.usecase.timeseries.TimeSeriesCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TimeSeriesWithValuesCalculationDefinition

// TODO this engine implementation is basedon TimeSeries.Split it or rename it.
abstract class RServeEngine<out R>(private val connectionProvider: RConnectionProvider) : Engine<TimeSeriesWithValuesCalculationDefinition, R> {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
        internal val baseUserScriptPath = "/var/userScripts/"
    }

    open fun beforeExecute(conn: RConnection, calcDef: TimeSeriesWithValuesCalculationDefinition) {
    }

    abstract fun afterExecute(conn: RConnection, result: REXP, calcDef: TimeSeriesWithValuesCalculationDefinition): R

    override fun call(calcDef: TimeSeriesWithValuesCalculationDefinition): R =
            try {
                val conn = connectionProvider.getConnection()
                log.debug("Evaluating " + calcDef)

                calcDef.sourceScript(conn)
                calcDef.prepareParameters(conn)

                log.debug("Calling script")
                val result = conn.eval("dfOut <- run(dfIn, additionalParameters)")

                afterExecute(conn, result, calcDef)
            } catch (ex: RserveException) {
                ex.printStackTrace()
                throw CalculationException(ex.message ?: "There was an error during calculation.")
            }
}

internal fun CalculationDefinition.sourceScript(conn: RConnection) {
    val path = """$baseUserScriptPath$calculationScriptPath.R"""
    log.debug("""Sourcing: $path""")
    conn.voidEval("""writeLines("##\nStarted execution of: $path\n##")""")
    conn.voidEval("""source("$path")""")
}

internal fun CalculationDefinition.prepareParameters(conn: RConnection) {
    conn.voidEval("additionalParameters <- data.frame(name = as.character(), value = as.character(), stringsAsFactors = FALSE)")
    conn.voidEval("""colnames(additionalParameters) <- c("name", "value")""")

    var rowIdx = 1
    for ((key, value) in additionalParameters) {
        conn.voidEval("""additionalParameters[$rowIdx,] <- c("$key", "$value")""")
        rowIdx++
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

internal fun REXP.processResults(calcDef: CalculationDefinition): RList =
        when {
            isList -> asList()
            isNumeric -> RList()
            else -> {
                log.warn("""Definition: $calcDef returned ${this.toDebugString()}""")
                RList()
            }
        }

internal fun TimeSeriesCalculationDefinition.partitionResults(dataFrame: RList): Pair<List<Pair<Symbol, REXP>>, List<Pair<Symbol, REXP?>>> {
    val (results, missingResults) = timeSeriesIdsOut
            .map { it.key to dataFrame[it.key] as REXP? }
            .partition { it.second != null }
    return Pair(results as List<Pair<Symbol, REXP>>, missingResults)
}
