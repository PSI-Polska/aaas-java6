package pl.psi.aaas.usecase

import com.nhaarman.mockito_kotlin.*
import io.kotlintest.specs.StringSpec

val ValidDefinition = CalculationDefinition(listOf(1, 2, 3), listOf(101, 102), "validScriptPath")


class ScriptExecutionTest : StringSpec() {
    init {
        val NoSynchronizationSynchronizer = mock<ScriptSynchronizer> {
            on { isUnderSynchronization() } doReturn false
        }
        val TsRepo = mock<TimeSeriesRepository> {
            on { read(any()) } doReturn arrayOf(10L, 11, 10, 11)
        }

        "Validation" {
            TODO("Validation cases")
        }

        "ScriptExecutioner checks with Synchronizer if it can run" {

            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo)

            out.call(ValidDefinition)

            verify(NoSynchronizationSynchronizer, times(1)).isUnderSynchronization()
        }

        "ScriptsExecutioner waits until synchronization is finished" {
            TODO("Synchronization cases")
        }

        "ScriptsExecutioner reads all time series defined in TsIn" {
            val out = JustScriptExecution(NoSynchronizationSynchronizer, TsRepo)

            out.call(ValidDefinition)

            verify(TsRepo, times(ValidDefinition.timeSeriesIdsIn.size)).read(any())
        }

    }
}