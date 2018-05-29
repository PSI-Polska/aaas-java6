package pl.psi.aaas.engine.r

import org.joda.time.DateTime
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.slf4j.LoggerFactory
import pl.psi.aaas.Engine
import pl.psi.aaas.engine.r.RServeEngine.Companion.baseUserScriptPath
import pl.psi.aaas.engine.r.RServeEngine.Companion.log
import pl.psi.aaas.engine.r.transceiver.RValuesTransceiverFactory
import pl.psi.aaas.usecase.*
import pl.psi.aaas.usecase.parameters.Column
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Vector
import pl.psi.aaas.usecase.timeseries.TSCalcDef
import pl.psi.aaas.usecase.timeseries.TSCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TSDataFrame

/**
 * TODO
 */
class RServeEngine<in D : CalculationDefinitonWithValues<V>, V>(private val connectionProvider: RConnectionProvider) : Engine<D, V, Parameters> {
    companion object {
        internal val log = LoggerFactory.getLogger(RServeEngine::class.java)
        internal val baseUserScriptPath = "/var/userScripts/"
    }

    // TODO 05.05.2018 kskitek: introduce a way to register and select proper transceiver
    // maybe extend the factory?
    override fun call(calcDef: D): Parameters? =
            try {
                val conn = connectionProvider.getConnection()
                val tsTransceiver = RValuesTransceiverFactory.get(conn)
                log.debug("Evaluating $calcDef")

                calcDef.sourceScript(conn)

                tsTransceiver.send("dfIn", calcDef.values as TSDataFrame, calcDef)
                // TODO we can remove the above line - use only inParameters?
                calcDef.inParameters.forEach {
                    val t = RValuesTransceiverFactory.get(it.value, conn)
                    t.send(it.key, it.value, calcDef)
                }

                debugR(calcDef.inParameters, conn)

                log.debug("Calling script")
                calcDef.outParameters.keys.forEach { conn.voidEval("$it <- c()") }
                val ret = conn.eval("dfOut <- run(dfIn, inParameters)")

                debugR(calcDef.outParameters, conn)
                val retMap = calcDef.outParameters.map { it.key to RValuesTransceiverFactory.get(it.value, conn) }
                        .map { it.first to it.second.receive(it.first, null, calcDef) }.toMap() as Parameters
                val p3DRetParams = getP3DParams(conn, calcDef)

                log.debug(retMap.entries.joinToString("\n"))
                val mutableRetMap = mutableMapOf<Symbol, Parameter<*>>()
                mutableRetMap.putAll(retMap)
                mutableRetMap.putAll(p3DRetParams)
                mutableRetMap

//                tsTransceiver.receive("dfOut", result, calcDef) as R?
            } catch (ex: RserveException) {
                ex.printStackTrace()
                throw CalculationException(ex.message ?: "There was an error during calculation.")
            }

    @Deprecated("This function is only valid for P3D")
    private fun getP3DParams(conn: RConnection, calcDef: D): Parameters {
        val dates = Parameter.ofArray(emptyArray<DateTime?>(), DateTime::class.java) as Vector<in Any>
        val values = Parameter.ofArray(emptyArray<Double?>(), Double::class.java) as Vector<in Any>
        val columns = arrayOf(Column("TSDATE_TZ", dates), Column("VALUES", values))
        val dataFrame = Parameter.ofDataFrame(columns)
        val newOutParameters = mapOf("dfOut" to dataFrame as Parameter<*>)

        return if (calcDef is TSCalculationDefinition) {
            val newCalcDef = TSCalcDef(calcDef.timeSeriesIdsIn, calcDef.timeSeriesIdsOut, calcDef.begin, calcDef.end,
                    calcDef.calculationScript, calcDef.inParameters, newOutParameters, calcDef.resolution)
            mapOf("dfOut" to RValuesTransceiverFactory.get(dataFrame, conn)
                    .receive("dfOut", null, newCalcDef) as Parameter<*>)
        } else {
            emptyMap()
        }
    }

    private fun debugR(parameters: Parameters, conn: RConnection) =
            parameters.map { it.key }
                    .forEach {
                        conn.voidEval("print(\"## $it\")")
                        conn.voidEval("str($it)")
                    }
}

private fun CalculationDefinition.sourceScript(conn: RConnection) {
    val path = """$baseUserScriptPath$calculationScript.R"""
    log.debug("""Sourcing: $path""")
    conn.voidEval("""writeLines("##\nStarted execution ofPrimitive: $path\n##")""")
    conn.voidEval("""source("$path")""")
}

