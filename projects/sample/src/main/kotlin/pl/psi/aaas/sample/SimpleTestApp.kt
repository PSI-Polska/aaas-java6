package pl.psi.aaas.sample

import pl.psi.aaas.Facade
import pl.psi.aaas.engine.REngineConfiguration
import pl.psi.aaas.engine.RServeEngine
import pl.psi.aaas.usecase.*
import java.time.ZonedDateTime

object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade = FixedFacade

        facade.callScript(prepCalcDef1())
    }

    fun prepCalcDef1(): CalculationDefinition {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("C" to 3L)
        val begin = ZonedDateTime.now()
        val end = begin.plusDays(1)

        return CalculationDefinition(inIds, outIds, begin, end, "/var/userScripts/add.R")
    }
}

object FixedFacade : Facade {
    val engine: Engine = RServeEngine(REngineConfiguration("192.168.99.100", 6311))
    val synchronizer: ScriptSynchronizer = NoSynchronizationSynchronizer()
    val tsRepository: TimeSeriesRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: CalculationDefinition) {
        val scriptExecution = JustScriptExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}