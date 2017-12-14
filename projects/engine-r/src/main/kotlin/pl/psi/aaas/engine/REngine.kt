package pl.psi.aaas.engine

import org.apache.logging.log4j.LogManager
import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS
import pl.psi.aaas.usecase.Symbol
import java.util.Collections.emptyList


class RServeEngine(private val configuration: REngineConfiguration) : Engine {
    private val log = LogManager.getLogger()

    override fun schedule(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS {
        val conn = getConnection()

        source(calcDef, conn)
        sendValues(tsValues, conn)
//        setAdditionalParameters()
        val resultDf = execute(conn).asList()
        logDataFrame(resultDf)
        return mapDataFrameToTS(resultDf, calcDef)
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
        val missingSymbols = missingResults.map { it.first }.joinToString()
        log.error("""Definition: ${calcDef.calculationScriptPath} did not return required symbols: $missingSymbols""")
    }

    private fun execute(conn: RConnection): REXP {
        log.debug("Calling script")
        return conn.eval("dfOut <- run(dfIn)")
        // TODO 13.12.2017 kskitek: handle exceptions here
    }

    private fun sendValues(tsValues: MappedTS, conn: RConnection) {
        val allVectors = tsValues.map { it.first }.joinToString()
        log.debug("""Sending values $allVectors""")
        tsValues.forEach { conn.assign(it.first, it.second) }
        conn.voidEval("""dfIn <- data.frame($allVectors)""")
    }

    private fun source(calcDef: CalculationDefinition, conn: RConnection) {
        log.debug("""Sourcing: ${calcDef.calculationScriptPath}""")
        conn.voidEval("""source("${calcDef.calculationScriptPath}")""")
    }

    private fun getConnection(): RConnection = RConnection(configuration.address, configuration.port)
}

data class REngineConfiguration(val address: String, val port: Int)