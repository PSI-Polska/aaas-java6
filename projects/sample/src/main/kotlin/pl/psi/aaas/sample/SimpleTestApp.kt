package pl.psi.aaas.sample

import pl.psi.aaas.Facade
import pl.psi.aaas.engine.NoSynchronizationSynchronizer
import pl.psi.aaas.engine.REngineConfiguration
import pl.psi.aaas.engine.RServeEngine
import pl.psi.aaas.usecase.*
import pl.psi.aaas.usecase.timeseries.TimeSeriesBasedCalculationExecution
import java.lang.management.ManagementFactory
import java.rmi.server.RMISocketFactory
import java.time.ZonedDateTime
import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import kotlin.concurrent.thread


object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade = FixedFacade
        initMBean()

//        fixedRateTimer(period = TimeUnit.SECONDS.toMillis(2)) {
            facade.callScript(prepCalcDef1())
//        }
    }

    private fun initMBean() {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val nameObject = ObjectName("pl.psi.aaas.usecase:type=Statistics")
        mbs.registerMBean(JmxStatistics, nameObject)
        initRmiJmx(mbs)
    }

    private fun initRmiJmx(mbs: MBeanServer?) {
        thread { RMISocketFactory.getDefaultSocketFactory().createServerSocket(9998) }
        val url = JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9998/server")
        val cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs)
        thread { cs.start() }
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
    val engine: Engine = RServeEngine(REngineConfiguration("localhost", 6311))
    val synchronizer: ScriptSynchronizer = NoSynchronizationSynchronizer()
    val tsRepository: TimeSeriesRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: CalculationDefinition) {
        val scriptExecution = TimeSeriesBasedCalculationExecution(engine = engine, synchronizer = synchronizer, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}