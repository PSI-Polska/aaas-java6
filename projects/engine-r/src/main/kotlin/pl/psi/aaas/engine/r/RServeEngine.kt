package pl.psi.aaas.engine.r

import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.slf4j.LoggerFactory
import pl.psi.aaas.Engine
import pl.psi.aaas.engine.r.RServeEngine.Companion.baseUserScriptPath
import pl.psi.aaas.engine.r.RServeEngine.Companion.log
import pl.psi.aaas.engine.r.timeseries.TSValuesTransceiver
import pl.psi.aaas.engine.r.transceiver.DateTimeTransceiver
import pl.psi.aaas.engine.r.transceiver.StringTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationDefinitonWithValues
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.parameters.DateTimeParam
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.StringParam
import pl.psi.aaas.usecase.timeseries.TSDataFrame

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
                val tsTransceiver = RValuesTransceiverFactory.get<D>(conn)
                log.debug("Evaluating $calcDef")

                calcDef.sourceScript(conn)

                tsTransceiver.send(calcDef.values as TSDataFrame, calcDef)
                // TODO we can remove the above line - use only parameters?
                calcDef.parameters.forEach {
                    val t = RValuesTransceiverFactory.get<D>(it, conn)
                    t.send(it, calcDef)
                }
//                calcDef.prepareParameters(conn) TODO

                log.debug("Calling script")
                val result = conn.eval("dfOut <- run(dfIn, parameters)")

                tsTransceiver.receive(result, calcDef) as R?
            } catch (ex: RserveException) {
                ex.printStackTrace()
                throw CalculationException(ex.message ?: "There was an error during calculation.")
            }
}

object RValuesTransceiverFactory {
    fun <D : CalculationDefinition> get(parameter: Parameter<*>, conn: RConnection): RValuesTransceiver<Parameter<*>, *, D> =
            when (parameter) {
                is StringParam   -> StringTransceiver<D>(conn)
                is DateTimeParam -> DateTimeTransceiver<D>(conn)
                else             -> throw CalculationException("Not implemented parameter type ${parameter.javaClass}")
            } as RValuesTransceiver<Parameter<*>, *, D>

    // TODO this should be removed when TSDataFrame is Parameter<??>
    fun <D : CalculationDefinition> get(conn: RConnection): RValuesTransceiver<TSDataFrame, TSDataFrame, D> {
//        inline fun <reified V, reified R, reified D : CalculationDefinition> create(conn: RConnection): RValuesTransceiver<V, R, D> {
        // TODO 05.05.2018 kskitek: handle different V and R types
//        if (V::class.nestedClasses )
        return TSValuesTransceiver(conn) as RValuesTransceiver<TSDataFrame, TSDataFrame, D>
    }

}

private fun CalculationDefinition.sourceScript(conn: RConnection) {
    val path = """$baseUserScriptPath$calculationScript.R"""
    log.debug("""Sourcing: $path""")
    conn.voidEval("""writeLines("##\nStarted execution of: $path\n##")""")
    conn.voidEval("""source("$path")""")
}

