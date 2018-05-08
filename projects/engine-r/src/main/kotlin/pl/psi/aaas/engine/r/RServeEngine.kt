package pl.psi.aaas.engine.r

import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.slf4j.LoggerFactory
import pl.psi.aaas.Engine
import pl.psi.aaas.engine.r.RServeEngine.Companion.baseUserScriptPath
import pl.psi.aaas.engine.r.RServeEngine.Companion.log
import pl.psi.aaas.engine.r.timeseries.TSValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationDefinitonWithValues
import pl.psi.aaas.usecase.CalculationException

/**
 * TODO
 */
class RServeEngine<in D : CalculationDefinitonWithValues<V>, V, out R>(private val connectionProvider: RConnectionProvider) : Engine<D, V, R> {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
        internal val baseUserScriptPath = "/var/userScripts/"
    }

    // TODO 05.05.2018 kskitek: introduce a way to register and select proper transceiver
    // maybe extend the factory?
    override fun call(calcDef: D): R? =
            try {
                val conn = connectionProvider.getConnection()
                val transceiver = RValuesTransceiverFactory.create<V, R, D>(conn)
                log.debug("Evaluating $calcDef")

                calcDef.sourceScript(conn)
                transceiver.send(calcDef.values, calcDef)
                calcDef.prepareParameters(conn)

                log.debug("Calling script")
                val result = conn.eval("dfOut <- run(dfIn, additionalParameters)")

                transceiver.receive(result, calcDef)
            } catch (ex: RserveException) {
                ex.printStackTrace()
                throw CalculationException(ex.message ?: "There was an error during calculation.")
            }
}

object RValuesTransceiverFactory {
    fun <V, R, D : CalculationDefinition> create(conn: RConnection): RValuesTransceiver<V, R, D> {
//        inline fun <reified V, reified R, reified D : CalculationDefinition> create(conn: RConnection): RValuesTransceiver<V, R, D> {
        // TODO 05.05.2018 kskitek: handle different V and R types
//        if (V::class.nestedClasses )
        return TSValuesTransceiver(conn) as RValuesTransceiver<V, R, D>
    }

}

private fun CalculationDefinition.sourceScript(conn: RConnection) {
    val path = """$baseUserScriptPath$calculationScript.R"""
    log.debug("""Sourcing: $path""")
    conn.voidEval("""writeLines("##\nStarted execution of: $path\n##")""")
    conn.voidEval("""source("$path")""")
}

private fun CalculationDefinition.prepareParameters(conn: RConnection) {
    conn.voidEval("additionalParameters <- data.frame(name = as.character(), value = as.character(), stringsAsFactors = FALSE)")
    conn.voidEval("""colnames(additionalParameters) <- c("name", "value")""")

    var rowIdx = 1
    for ((key, value) in additionalParameters) {
        conn.voidEval("""additionalParameters[$rowIdx,] <- c("$key", "$value")""")
        rowIdx++
    }
}
