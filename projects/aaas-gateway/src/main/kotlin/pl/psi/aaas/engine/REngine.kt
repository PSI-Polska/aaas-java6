package pl.psi.aaas.engine

import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS
import java.util.logging.Logger

internal class RServeEngine(val configuration: REngineConfiguration) : Engine {
    private val log = Logger.getLogger(this.javaClass.name)

    override fun schedule(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS {
        val conn = getConnection()

        source(calcDef, conn)
        sendValues(tsValues, conn)
//        setAdditionalParameters()
        val result = execute(conn)
        val doubles = result.asDoubles() // TODO 13.12.2017 kskitek: what if result is vector?!

        return listOf("X" to doubles)
    }

    private fun execute(conn: RConnection): REXP {
        log.fine("Calling script")
        return conn.eval("dfOut <- run(dfIn)")
        // TODO 13.12.2017 kskitek: handle exceptions here
    }

    private fun sendValues(tsValues: MappedTS, conn: RConnection) {
        tsValues.forEach { conn.assign(it.first, it.second) }
    }

    private fun source(calcDef: CalculationDefinition, conn: RConnection) {
        log.fine("""Sourcing: ${calcDef.calculationScriptPath} """)
        conn.voidEval("""source("${calcDef.calculationScriptPath}")""")
    }

    private fun getConnection(): RConnection = RConnection(configuration.address, configuration.port)
}

data class REngineConfiguration(val address: String, val port: Int)