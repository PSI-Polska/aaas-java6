package pl.psi.aaas.sample

import pl.psi.aaas.Facade
import pl.psi.aaas.StatisticsEngine
import pl.psi.aaas.StatsDConfig
import pl.psi.aaas.engine.NoSynchronizationSynchronizer
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.usecase.*
import pl.psi.aaas.usecase.timeseries.TimeSeriesBasedCalculationExecution
import pl.psi.aaas.usecase.timeseries.TimeSeriesRepository
import java.lang.management.ManagementFactory
import java.rmi.server.RMISocketFactory
import java.time.ZonedDateTime


object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade = FixedFacade
        facade.callScript(prepCalcDef1())
    }

    private fun prepCalcDef1(): CalculationDefinition {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("C" to 3L)
        val begin = ZonedDateTime.now()
        val end = begin.plusDays(1)

        return CalculationDefinition(inIds, outIds, begin, end, "add")
    }
}

val LocalConfiguration = REngineConfiguration("localhost", 6311)

class LocalRConnectionProvider(override var configuration: REngineConfiguration = LocalConfiguration) : RConnectionProvider

object FixedFacade : Facade {
    private val engine: Engine = StatisticsEngine(RServeEngine(LocalRConnectionProvider()), StatsDConfig("localhost", 8125))
    private val synchronizer: ScriptSynchronizer = NoSynchronizationSynchronizer()
    private val tsRepository: TimeSeriesRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: CalculationDefinition) {
        val scriptExecution = TimeSeriesBasedCalculationExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}