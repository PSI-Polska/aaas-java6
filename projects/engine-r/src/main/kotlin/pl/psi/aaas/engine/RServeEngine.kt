package pl.psi.aaas.engine

import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import org.slf4j.LoggerFactory
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS
import pl.psi.aaas.usecase.Symbol
import java.util.Collections.emptyList

// TODO this engine implementation is basedon TimeSeries.Split it or rename it.
class RServeEngine(private val connectionProvider: RConnectionProvider) : Engine {
    private val log = LoggerFactory.getLogger(RServeEngine::class.java)

    companion object {
        private val baseUserScriptPath = "/var/userScripts/"
    }

    override fun call(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS {
        val conn = connectionProvider.getConnection()

        source(calcDef, conn)
        sendValues(tsValues, conn)
//        setAdditionalParameters(calcDef, conn)
        val resultDf = execute(conn).asList()
        logDataFrame(resultDf)
        return mapDataFrameToTS(resultDf, calcDef)
    }

    private fun setAdditionalParameters(calcDef: CalculationDefinition, conn: RConnection) {
        TODO("not implemented")
    }

    private fun logDataFrame(resultDf: RList) {
        val dfColumns = resultDf.keys().joinToString()
        log.debug("""Result DF columns: $dfColumns""")
    }

    private fun mapDataFrameToTS(resultDf: RList, calcDef: CalculationDefinition): MappedTS {
        val (results, missingResults) = calcDef.timeSeriesIdsOut
                .map { it.key to resultDf[it.key] as REXP? }
                .partition { it.second != null }

        return if (missingResults.isNotEmpty()) {
            logMissingResults(missingResults, calcDef)
            // TODO 14.12.2017 kskitek: throw exception mby?!
            emptyList()
        } else results.map { it.first to it.second!!.asDoubles() }

    }

    private fun logMissingResults(missingResults: List<Pair<Symbol, REXP?>>, calcDef: CalculationDefinition) {
        val missingSymbols = missingResults.joinToString { it.first }
        log.error("""Definition: ${calcDef.calculationScriptPath} did not return required symbols: $missingSymbols""")
    }

    private fun execute(conn: RConnection): REXP {
        log.debug("Calling script")
        return conn.eval("dfOut <- run(dfIn)")
        // TODO 13.12.2017 kskitek: handle exceptions here
    }

    private fun sendValues(tsValues: MappedTS, conn: RConnection) {
        val allVectors = tsValues.joinToString { it.first }
        log.debug("""Sending values $allVectors""")
        tsValues.forEach { conn.assign(it.first, it.second) }
        conn.voidEval("""dfIn <- data.frame($allVectors)""")
    }

    private fun source(calcDef: CalculationDefinition, conn: RConnection) {
        val path = """$baseUserScriptPath${calcDef.calculationScriptPath}.R"""
        log.debug("""Sourcing: $path""")
        conn.voidEval("""source("$path")""")
    }
}

data class REngineConfiguration(val address: String, val port: Int)

interface RConnectionProvider {
    var configuration: REngineConfiguration
    fun getConnection(): RConnection = RConnection(configuration.address, configuration.port)
}
