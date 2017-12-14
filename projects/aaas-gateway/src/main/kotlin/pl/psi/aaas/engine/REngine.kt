package pl.psi.aaas.engine

import org.rosuda.REngine.REXP
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS
import java.util.logging.Logger


internal class RServeEngine(private val configuration: REngineConfiguration) : Engine {
    private val log = Logger.getLogger(this.javaClass.name)

    override fun schedule(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS {
        val conn = getConnection()

        source(calcDef, conn)
        sendValues(tsValues, conn)
//        setAdditionalParameters()
        val resultDf = execute(conn).asList()
        return mapDfToTS(resultDf, calcDef)
    }

    private fun mapDfToTS(resultDf: RList, calcDef: CalculationDefinition): MappedTS {
        val (results, noResults) = calcDef.timeSeriesIdsOut
                .map { it.key to resultDf[it.key] as REXP? }
                .partition { it.second != null }

        return if (noResults.isNotEmpty()) {
            log.severe("""Definition: ${calcDef.calculationScriptPath} did not return required symbols: $noResults""")
            // TODO 14.12.2017 kskitek: throw exception mby?!
            emptyList()
        } else results.map { it.first to it.second!!.asDoubles() }

    }

    private fun execute(conn: RConnection): REXP {
        log.fine("Calling script")
        return conn.eval("dfOut <- run(dfIn)")
        // TODO 13.12.2017 kskitek: handle exceptions here
    }

    private fun sendValues(tsValues: MappedTS, conn: RConnection) {
        tsValues.forEach { conn.assign(it.first, it.second) }
        val allVectors = tsValues.map { it.first }.joinToString()
        conn.voidEval("""dfIn <- data.frame($allVectors)""")
    }

    private fun source(calcDef: CalculationDefinition, conn: RConnection) {
        log.fine("""Sourcing: ${calcDef.calculationScriptPath}""")
        conn.voidEval("""source("${calcDef.calculationScriptPath}")""")
    }

    private fun getConnection(): RConnection = RConnection(configuration.address, configuration.port)
}

data class REngineConfiguration(val address: String, val port: Int)