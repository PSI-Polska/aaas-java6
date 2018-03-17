package pl.psi.aaas

import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.timeseries.TimeSeriesRServeEngine
import pl.psi.aaas.usecase.ScriptSynchronizer
import pl.psi.aaas.usecase.timeseries.TimeSeriesBasedCalculationExecution
import pl.psi.aaas.usecase.timeseries.TimeSeriesCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TimeSeriesRepository
import javax.annotation.PostConstruct
import javax.ejb.Singleton
import javax.ejb.Startup
import javax.inject.Inject

@Startup
@Singleton
class CdiFacade : Facade<TimeSeriesCalculationDefinition> {
    @Inject
    lateinit var tsRepository: TimeSeriesRepository
    @Inject
    lateinit var synchronizer: ScriptSynchronizer
    @Inject
    lateinit var connection: RConnectionProvider

    override fun callScript(calcDef: TimeSeriesCalculationDefinition) {
        val engine = TimeSeriesRServeEngine(connection)
        val scriptExecution = TimeSeriesBasedCalculationExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }

    @PostConstruct
    private fun init() {

    }
}