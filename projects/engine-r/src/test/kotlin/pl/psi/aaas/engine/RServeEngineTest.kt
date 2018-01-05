package pl.psi.aaas.engine

import com.nhaarman.mockito_kotlin.verify
import io.kotlintest.specs.StringSpec
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Engine
import pl.psi.aaas.usecase.MappedTS
import java.time.ZonedDateTime


class RServeEngineTest : StringSpec() {

    private val ValidDefinition = CalculationDefinition(
            mapOf("A" to 1L, "B" to 2L, "C" to 3L),
            mapOf("Y" to 101L, "Z" to 102L),
            ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1),
            "validScriptPath")

    private val TS1M = "A" to doubleArrayOf(1.0, 1.0, 1.0, 1.0)
    private val TS2M = "B" to doubleArrayOf(2.0, 2.0, 2.0, 2.0)

    private val inTS: MappedTS = listOf(TS1M, TS2M)

    init {
        val connectionProvider = MockRConnectionProvider()
        val out: Engine = RServeEngine(connectionProvider)
        val conn = connectionProvider.getConnection()

        "Engine passes script path" {
            out.call(ValidDefinition, inTS)

            verify(conn).voidEval("""source("${ValidDefinition.calculationScriptPath}")""")
        }.config(enabled = false)

        "Engine sends all values" {
        }.config(enabled = false)

        "Engine executes a run method" {
            out.call(ValidDefinition, inTS)

            verify(conn).eval("dfOut <- run(dfIn)")
        }

        "Engine returns MappedTS" {
        }.config(enabled = false)

        "Engine throws an exception when required out symbols were not returned" {
        }.config(enabled = false)

        "Engine fixes script path to have base path and '.R' extension" {
        }.config(enabled = false)

        "Engine checks script path for root path or .." {
        }.config(enabled = false)
    }
}