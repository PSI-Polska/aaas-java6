package pl.psi.aaas.usecase

import com.nhaarman.mockito_kotlin.*
import io.kotlintest.specs.StringSpec
import pl.psi.aaas.usecase.timeseries.TimeSeriesBasedCalculationExecution

class CalculationExecutionTest : StringSpec() {
    init {
        val NoSynchronizationSynchronizer = mock<ScriptSynchronizer> {
            on { isUnderSynchronization() } doReturn false
        }
        val TsRepo = mock<TimeSeriesRepository> {
            on { read(any(), any(), any()) } doReturn doubleArrayOf()
            on { read(eq(1L), any(), any()) } doReturn TS1
            on { read(eq(2L), any(), any()) } doReturn TS2
            on { read(eq(3L), any(), any()) } doReturn TS3
        }
        val MockEngine = mock<Engine> {
            on { call(any(), any()) } doReturn listOf(TS1ResM, TS2ResM)
        }

        "Validation" {
            TODO("Validation cases")
        }.config(enabled = false)

        "ScriptExecutioner checks with Synchronizer if it can run" {

            val out = TimeSeriesBasedCalculationExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(NoSynchronizationSynchronizer).isUnderSynchronization()
        }

        "ScriptsExecutioner waits until synchronization is finished" {
            TODO("Synchronization cases")
        }.config(enabled = false)

        "ScriptsExecutioner reads all time series defined in TsIn" {
            val out = TimeSeriesBasedCalculationExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(TsRepo, times(ValidDefinition.timeSeriesIdsIn.size)).read(any(), any(), any())
        }

        "ScriptExecutioner fails when TS reading fails" {
            TODO()
        }.config(enabled = false)

        "ScriptExecutioner schedules calculation with mapped time series" {
            val out = TimeSeriesBasedCalculationExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)
            val expectedMappedTS = listOf("A" to TS1, "B" to TS2, "C" to TS3)

            out.call(ValidDefinition)

            verify(MockEngine).call(ValidDefinition, expectedMappedTS)
        }

        "ScriptExecutioner fails when Engine fails" {
            TODO()
        }.config(enabled = false)

        "ScriptExecutioner maps and saves returned from Engine data" {
            val out = TimeSeriesBasedCalculationExecution(NoSynchronizationSynchronizer, TsRepo, MockEngine)

            out.call(ValidDefinition)

            verify(TsRepo).save(101L, TS1Res)
            verify(TsRepo).save(102L, TS2Res)
        }

        "ScriptExecutioner fails when TS saving fails" {
            TODO()
        }.config(enabled = false)
    }
}