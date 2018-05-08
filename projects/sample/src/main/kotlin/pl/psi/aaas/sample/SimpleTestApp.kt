package pl.psi.aaas.sample

import pl.psi.aaas.Engine
import pl.psi.aaas.Facade
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.usecase.timeseries.*
import java.time.ZonedDateTime

object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade<TSCalculationDefinition> = FixedTSFacade
        facade.callScript(prepCalcDef1())
    }

    private fun prepCalcDef1(): TSCalcDef {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("C" to 3L)
        val begin = ZonedDateTime.now()
        val end = begin.plusDays(1)

        return TSCalcDef(inIds, outIds, begin, end, "add")
    }
}

val localConfiguration = REngineConfiguration("localhost", 6311)

class LocalRConnectionProvider(override var configuration: REngineConfiguration = localConfiguration) : RConnectionProvider

object FixedTSFacade : Facade<TSCalculationDefinition> {
    private val engine: Engine<TSCalcDefWithValues, TSDataFrame, TSDataFrame> = RServeEngine(LocalRConnectionProvider())
    private val tsRepository: TSRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: TSCalculationDefinition) {
        val scriptExecution = TSCalculationExecution(engine = engine, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}