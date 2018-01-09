package pl.psi.aaas.engine

import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.slf4j.LoggerFactory
import pl.psi.aaas.engine.RServeEngine.Companion.baseUserScriptPath
import pl.psi.aaas.engine.RServeEngine.Companion.log
import pl.psi.aaas.usecase.*

// TODO this engine implementation is basedon TimeSeries.Split it or rename it.
class RServeEngine(private val connectionProvider: RConnectionProvider) : Engine {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
        internal val baseUserScriptPath = "/var/userScripts/"
    }

    override fun call(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS {
        val conn = connectionProvider.getConnection()
        log.debug("Evaluating " + calcDef)

        calcDef.sourceScript(conn)
        tsValues.sendValues(conn)
        calcDef.prepareParameters(conn)
        val resultDf = execute(conn).asList()
        resultDf.logRList()
        return resultDf.mapDataFrameToTS(calcDef)
    }

    private fun execute(conn: RConnection): REXP =
            try {
                log.debug("Calling script")
                conn.eval("dfOut <- run(dfIn, additionalParameters)")
            } catch (ex: RserveException) {
                throw CalculationException(ex.message ?: "Calculation failed", ex)
            }
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

internal fun CalculationDefinition.sourceScript(conn: RConnection) {
    val path = """$baseUserScriptPath${calculationScriptPath}.R"""
    log.debug("""Sourcing: $path""")
    conn.voidEval("""source("$path")""")
}


internal fun MappedTS.sendValues(conn: RConnection) {
    val allVectors = joinToString { it.first }
    log.debug("""Sending values $allVectors""")
    forEach { conn.assign(it.first, it.second) }
    conn.voidEval("""dfIn <- data.frame($allVectors)""")
}

internal fun RList.logRList() {
    val dfColumns = keys().joinToString()
    log.debug("""Result DF columns: $dfColumns""")
}

internal fun RList.mapDataFrameToTS(calcDef: CalculationDefinition): MappedTS {
    val (results, missingResults) = calcDef.partitionResults(this)

    if (missingResults.isNotEmpty()) {
        val missingSymbols = missingResults.joinToString { it.first }
        log.error("""Definition: ${calcDef.calculationScriptPath} did not return required symbols: $missingSymbols""")
        throw CalculationException("Script did not return expected results: " + missingResults)
    } else {
        return results.map { it.first to it.second.asDoubles() }
    }

}

internal fun CalculationDefinition.partitionResults(dataFrame: RList): Pair<List<Pair<Symbol, REXP>>, List<Pair<Symbol, REXP?>>> {
    val (results, missingResults) = timeSeriesIdsOut
            .map { it.key to dataFrame[it.key] as REXP? }
            .partition { it.second != null }
    return Pair(results as List<Pair<Symbol, REXP>>, missingResults)
}
