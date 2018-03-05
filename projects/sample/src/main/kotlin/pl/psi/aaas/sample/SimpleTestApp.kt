package pl.psi.aaas.sample

import pl.psi.aaas.Facade
import pl.psi.aaas.engine.NoSynchronizationSynchronizer
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import pl.psi.aaas.engine.r.timeseries.TimeSeriesRServeEngine
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.ScriptSynchronizer
import pl.psi.aaas.usecase.timeseries.*
import java.time.ZonedDateTime

object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade<TimeSeriesCalculationDefinition> = FixedTSFacade
        facade.callScript(prepCalcDef1())
    }

    private fun prepCalcDef1(): TSCalcDefDTO {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("C" to 3L)
        val begin = ZonedDateTime.now()
        val end = begin.plusDays(1)

        return TSCalcDefDTO(inIds, outIds, begin, end, "add")
    }
}

val localConfiguration = REngineConfiguration("localhost", 6311)

class LocalRConnectionProvider(override var configuration: REngineConfiguration = localConfiguration) : RConnectionProvider

object FixedTSFacade : Facade<TimeSeriesCalculationDefinition> {
    private val engine: Engine<TimeSeriesWithValuesCalculationDefinition, MappedTS> = TimeSeriesRServeEngine(LocalRConnectionProvider())
    private val synchronizer: ScriptSynchronizer = NoSynchronizationSynchronizer()
    private val tsRepository: TimeSeriesRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: TimeSeriesCalculationDefinition) {
        val scriptExecution = TimeSeriesBasedCalculationExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}