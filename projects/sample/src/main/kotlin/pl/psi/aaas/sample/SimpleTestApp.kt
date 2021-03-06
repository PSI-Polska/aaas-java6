package pl.psi.aaas.sample

import org.joda.time.DateTime
import pl.psi.aaas.Engine
import pl.psi.aaas.Facade
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import pl.psi.aaas.engine.r.RServeEngine
import pl.psi.aaas.usecase.Parameters
import pl.psi.aaas.usecase.Symbol
import pl.psi.aaas.usecase.parameters.Column
import pl.psi.aaas.usecase.parameters.Parameter
import pl.psi.aaas.usecase.parameters.Vector
import pl.psi.aaas.usecase.timeseries.*

// TODO make integration tests from this class....
object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val facade: Facade<TSCalculationDefinition> = FixedTSFacade
        facade.callScript(prepCalcDef1())
    }

    private fun prepCalcDef1(): TSCalcDef {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("dfOut" to 3L)
//        val outIds = mapOf("C" to 3L)
        val begin = DateTime.now()
        val end = begin.plusDays(1)

        val strArr = arrayOf("a", "b", "c")
        val dtArr = arrayOf(DateTime.now().minusHours(2), DateTime.now().minusHours(1), DateTime.now())
        val strVec = Parameter.ofArrayNotNull(strArr, String::class.java)
        val dtVec = Parameter.ofArrayNotNull(dtArr, DateTime::class.java)
        val longVec = Parameter.ofArrayNotNull(arrayOf(1, 2, 3L), Long::class.java)
        val doubleVec = Parameter.ofArrayNotNull(arrayOf(0.1, 0.2, 1.0), Double::class.java)
        val doubleNullVec = Parameter.ofArray(arrayOf(0.1, null, 1.0), Double::class.java)
        val boolVec = Parameter.ofArrayNotNull(arrayOf(true, false, true), Boolean::class.java)
        val boolNullVec = Parameter.ofArray(arrayOf(true, false, null), Boolean::class.java)
        val dfColumns = arrayOf(Column("dt", dtVec as Vector<Any>), Column("longs", longVec as Vector<Any>), Column("doubles", doubleVec as Vector<Any>))
        val dfColumnClasses = arrayOf(DateTime::class.java, Long::class.java, Double::class.java) as Array<Class<Any>>

        val parameters = mutableMapOf(
                "str" to Parameter.ofPrimitive("str_value")
                , "dt" to Parameter.ofPrimitive(DateTime.now())
                , "d" to Parameter.ofPrimitive(0.75)
                , "l" to Parameter.ofPrimitive(10L)
                , "b" to Parameter.ofPrimitive(false)
                , "strV" to strVec
                , "dtV" to dtVec
                , "longV" to longVec
                , "doubleV" to doubleVec
                , "doubleNullV" to doubleNullVec
                , "boolV" to boolVec
                , "boolNullV" to boolNullVec
                , "df" to Parameter.ofDataFrame(dfColumns, dfColumnClasses)
        )

        return TSCalcDef(inIds, outIds, begin, end, "add",
                parameters, mutableMapOf())
    }
}

val localConfiguration = REngineConfiguration("localhost", 6311)

class LocalRConnectionProvider(override var configuration: REngineConfiguration = localConfiguration) : RConnectionProvider

object FixedTSFacade : Facade<TSCalculationDefinition> {
    private val engine: Engine<TSCalcDefWithValues, TSDataFrame, Parameters> = RServeEngine(LocalRConnectionProvider())
    private val tsRepository: TSRepository = MockTimeSeriesRepository()

    override fun callScript(calcDef: TSCalculationDefinition) {
        val scriptExecution = TSCalculationExecution(engine = engine, tsRepository = tsRepository)

        scriptExecution.call(calcDef)
    }
}