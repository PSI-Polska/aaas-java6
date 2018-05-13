package pl.psi.aaas.engine

import com.nhaarman.mockito_kotlin.verify
import io.kotlintest.specs.StringSpec
import pl.psi.aaas.engine.r.timeseries.TimeSeriesRServeEngine
import pl.psi.aaas.Engine
import pl.psi.aaas.usecase.timeseries.MappedTS
import pl.psi.aaas.usecase.timeseries.TSCalcDefWithValuesDTO
import pl.psi.aaas.usecase.timeseries.TimeSeriesWithValuesCalculationDefinition
import java.time.ZonedDateTime


class RServeEngineTest : StringSpec() {

    private val TS1M = "A" to doubleArrayOf(1.0, 1.0, 1.0, 1.0)
    private val TS2M = "B" to doubleArrayOf(2.0, 2.0, 2.0, 2.0)

    private val ValidDefinition = TSCalcDefWithValuesDTO(
            mapOf("A" to 1L, "B" to 2L, "C" to 3L),
            mapOf("Y" to 101L, "Z" to 102L),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1),
            "validScriptPath",
            emptyMap(),
            listOf(TS1M, TS2M))

    private val inTS: MappedTS = listOf(TS1M, TS2M)

    init {
        val connectionProvider = MockRConnectionProvider()
        val out: Engine<TimeSeriesWithValuesCalculationDefinition, MappedTS> = TimeSeriesRServeEngine(connectionProvider)
        val conn = connectionProvider.getConnection()

        "Engine passes script path" {
            out.call(ValidDefinition)

            verify(conn).voidEval("""source("${ValidDefinition.calculationScript}")""")
        }.config(enabled = false)

        "Engine sends all values" {
        }.config(enabled = false)

        "Engine executes a run method" {
            out.call(ValidDefinition)

            verify(conn).eval("dfOut <- run(dfIn, inParameters)")
        }

        "Engine returns MappedTS" {
        }.config(enabled = false)

        "Engine throws an exception when required out symbols were not returned" {
        }.config(enabled = false)

        "Engine fixes script path to have base path and '.R' extension" {
        }.config(enabled = false)

        "Engine checks script path for root path or .." {
        }.config(enabled = false)

        "Execution engine can be RList()" {
        }.config(enabled = false)

        "Execution engine can be numeric value" {
        }.config(enabled = false)
    }
}