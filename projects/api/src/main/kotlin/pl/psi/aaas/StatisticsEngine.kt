package pl.psi.aaas

import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.StatsDClient
import pl.psi.aaas.StatisticsEngine.Companion.EXEC_SUCC
import pl.psi.aaas.StatisticsEngine.Companion.EXEC_TIME
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS

class StatisticsEngine(private val engine: Engine, config: StatsDConfig) : Engine by engine {
    private val stats: StatsDClient = NonBlockingStatsDClient(engine.javaClass.simpleName, config.hostname, config.port)

    companion object {
        internal const val CURR_EXECUTIONS = "current_executions"
        internal const val EXEC_FAILED = "executions_failed"
        internal const val EXEC_SUCC = "executions_succeeded"
        internal const val EXEC_TIME = "execution_time"
    }

    override fun call(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS =
            try {
                stats.recordGaugeDelta(CURR_EXECUTIONS, 1)
                stats.recordSetEvent("executed_script", calcDef.calculationScriptPath)

                stats.recordTime { engine.call(calcDef, tsValues) }
            } catch (ex: CalculationException) {
                stats.incrementCounter(EXEC_FAILED)
                throw ex
            } finally {
                stats.recordGaugeDelta(CURR_EXECUTIONS, -1)
//                stats.stop()
            }
}

data class StatsDConfig(val hostname: String, val port: Int)

fun StatsDClient.recordTime(block: () -> MappedTS): MappedTS {
    val start = System.currentTimeMillis()
    val value = block()
    val end = System.currentTimeMillis()
    recordExecutionTime(EXEC_TIME, end - start)
    count(EXEC_SUCC, 1)
    return value
}
