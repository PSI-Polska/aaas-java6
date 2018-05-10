package pl.psi.aaas.sample

import pl.psi.aaas.Engine
import pl.psi.aaas.Facade
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.usecase.parameters.Column
import pl.psi.aaas.usecase.parameters.Parameter
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

        val arr = arrayOf("a", "b", "c")
        val dtArr = arrayOf(ZonedDateTime.now().minusHours(2), ZonedDateTime.now().minusHours(1), ZonedDateTime.now())
        val c = Parameter.ofNN(arr, Array<String>::class.java, String::class.java)
        val d = Parameter.ofNN(dtArr, Array<ZonedDateTime>::class.java, ZonedDateTime::class.java)
        val e = Parameter.ofNN(arrayOf(1,2,3L), Array<Long>::class.java,Long::class.java)
        val parameters = mapOf(
                "a" to Parameter.ofPrimitive("str")
                , "B" to Parameter.ofPrimitive(ZonedDateTime.now())
                , "C" to c
                , "D" to d
                , "E" to e
                , "df" to Parameter.of(arrayOf("A" to c, "B" to d) as Array<Column>)
        )

        return TSCalcDef(inIds, outIds, begin, end, "add", parameters)
    }
}

val localConfiguration = REngineConfiguration("192.168.99.100", 6311)

class LocalRConnectionProvider(override var configuration: REngineConfiguration = localConfiguration) : RConnectionProvider

object FixedTSFacade : Facade<TSCalculationDefinition> {
    private val engine: Engine<TSCalcDefWithValues, TSDataFrame, TSDataFrame> = RServeEngine(LocalRConnectionProvider())
    private val tsRepository: TSRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: TSCalculationDefinition) {
        val scriptExecution = TSCalculationExecution(engine = engine, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}