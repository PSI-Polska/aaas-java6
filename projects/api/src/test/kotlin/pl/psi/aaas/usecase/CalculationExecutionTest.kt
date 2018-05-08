package pl.psi.aaas.usecase

import com.nhaarman.mockito_kotlin.*
import io.kotlintest.specs.StringSpec
import pl.psi.aaas.Engine
import pl.psi.aaas.usecase.timeseries.MappedTS
import pl.psi.aaas.usecase.timeseries.VectorCalculationExecution
import pl.psi.aaas.usecase.timeseries.TimeSeriesRepository
import pl.psi.aaas.usecase.timeseries.TimeSeriesWithValuesCalculationDefinition

class CalculationExecutionTest : StringSpec() {
    init {
        val noSynchronizationSynchronizer = mock<ScriptSynchronizer> {
            on { isUnderSynchronization() } doReturn false
        }
        val tsRepo = mock<TimeSeriesRepository> {
            on { read(any(), any(), any()) } doReturn doubleArrayOf()
            on { read(eq(1L), any(), any()) } doReturn TS1
            on { read(eq(2L), any(), any()) } doReturn TS2
            on { read(eq(3L), any(), any()) } doReturn TS3
        }
        val mockEngine = mock<Engine<TimeSeriesWithValuesCalculationDefinition, MappedTS>> {
            on { call(any()) } doReturn listOf(TS1ResM, TS2ResM)
        }

        "Validation" {
            TODO("Validation cases")
        }.config(enabled = false)

        "ScriptExecutioner checks with Synchronizer if it can run" {

            val out = VectorCalculationExecution(noSynchronizationSynchronizer, tsRepo, mockEngine)

            out.call(ValidDefinition)

            verify(noSynchronizationSynchronizer).isUnderSynchronization()
        }

        "ScriptsExecutioner waits until synchronization is finished" {
            TODO("Synchronization cases")
        }.config(enabled = false)

        "ScriptsExecutioner reads all time series defined in TsIn" {
            val out = VectorCalculationExecution(noSynchronizationSynchronizer, tsRepo, mockEngine)

            out.call(ValidDefinition)

            verify(tsRepo, times(ValidDefinitionWithTS.timeSeriesIdsIn.size)).read(any(), any(), any())
        }

        "ScriptExecutioner fails when TS reading fails" {
            TODO()
        }.config(enabled = false)

        "ScriptExecutioner schedules calculation with mapped time series" {
            val out = VectorCalculationExecution(noSynchronizationSynchronizer, tsRepo, mockEngine)
            val expectedMappedTS = listOf("A" to TS1, "B" to TS2, "C" to TS3)

            out.call(ValidDefinition)

            verify(mockEngine).call(ValidDefinitionWithTS)
        }

        "ScriptExecutioner fails when Engine fails" {
            TODO()
        }.config(enabled = false)

        "ScriptExecutioner maps and saves returned from Engine data" {
            val out = VectorCalculationExecution(noSynchronizationSynchronizer, tsRepo, mockEngine)

            out.call(ValidDefinition)

            verify(tsRepo).save(101L, ValidDefinitionWithTS.begin, TS1Res)
            verify(tsRepo).save(102L, ValidDefinitionWithTS.begin, TS2Res)
        }

        "ScriptExecutioner fails when TS saving fails" {
            TODO()
        }.config(enabled = false)
    }
}