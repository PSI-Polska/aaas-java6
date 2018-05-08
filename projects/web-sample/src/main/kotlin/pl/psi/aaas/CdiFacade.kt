package pl.psi.aaas

import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.timeseries.TimeSeriesRServeEngine
import pl.psi.aaas.usecase.ScriptSynchronizer
import pl.psi.aaas.usecase.timeseries.VectorCalculationExecution
import pl.psi.aaas.usecase.timeseries.TSCalculationDefinition
import pl.psi.aaas.usecase.timeseries.TimeSeriesRepository
import javax.annotation.PostConstruct
import javax.ejb.Singleton
import javax.ejb.Startup
import javax.inject.Inject

@Startup
@Singleton
class CdiFacade : Facade<TSCalculationDefinition> {
    @Inject
    lateinit var tsRepository: TimeSeriesRepository
    @Inject
    lateinit var synchronizer: ScriptSynchronizer
    @Inject
    lateinit var connection: RConnectionProvider

    override fun callScript(calcDef: TSCalculationDefinition) {
        val engine = TimeSeriesRServeEngine(connection)
        val scriptExecution = VectorCalculationExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }

    @PostConstruct
    private fun init() {

    }
}