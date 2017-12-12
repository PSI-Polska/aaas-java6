package pl.psi.aaas.usecase

import com.nhaarman.mockito_kotlin.*
import io.kotlintest.specs.StringSpec

val ValidDefinition = CalculationDefinition(
        mapOf("A" to 1L, "B" to 2L, "C" to 3L),
        mapOf("Y" to 101L, "Z" to 102L),
        "validScriptPath")

val TS1 = arrayOf(1L, 1, 1, 1)
val TS1M = "A" to TS1
val TS2 = arrayOf(2L, 2, 2, 2)
val TS2M = "B" to TS2
val TS3 = arrayOf(3L, 3, 3, 3)
val TS3M = "C" to TS3

val TS1Res = arrayOf(-1L, -1, -1, -1)
val TS1ResM = "Y" to TS1Res
val TS2Res = arrayOf(-2L, -2, -2, -2)
val TS2ResM = "Z" to TS2Res

class ScriptExecutionTest : StringSpec() {
    init {
        val NoSynchronizationSynchronizer = mock<ScriptSynchronizer> {
            on { isUnderSynchronization() } doReturn false
        }
        val TsRepo = mock<TimeSeriesRepository> {
            on { read(any()) } doReturn arrayOf()
            on { read(eq(1L)) } doReturn TS1
            on { read(eq(2L)) } doReturn TS2
            on { read(eq(3L)) } doReturn TS3
        }
        val MockEngine = mock<Engine> {
            on { schedule(any(), any()) } doReturn listOf(TS1ResM, TS2ResM)
        }

        "Validation" {
            TODO("Validation cases")
        }

        "ScriptExecutioner checks with Synchronizer if it can run" {

            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(NoSynchronizationSynchronizer).isUnderSynchronization()
        }

        "ScriptsExecutioner waits until synchronization is finished" {
            TODO("Synchronization cases")
        }

        "ScriptsExecutioner reads all time series defined in TsIn" {
            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(TsRepo, times(ValidDefinition.timeSeriesIdsIn.size)).read(any())
        }

        "ScriptExecutioner fails when TS reading fails" {
            TODO()
        }

        "ScriptExecutioner schedules calculation with mapped time series" {
            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)
            val expectedMappedTS = listOf("A" to TS1, "B" to TS2, "C" to TS3)

            out.call(ValidDefinition)

            verify(MockEngine).schedule(ValidDefinition, expectedMappedTS)
        }

        "ScriptExecutioner fails when Engine fails" {
            TODO()
        }

        "ScriptExecutioner maps and saves returned from Engine data" {
            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(TsRepo).save(101L, TS1Res)
            verify(TsRepo).save(102L, TS2Res)
        }

        "ScriptExecutioner fails when TS saving fails" {
            TODO()
        }
    }
}