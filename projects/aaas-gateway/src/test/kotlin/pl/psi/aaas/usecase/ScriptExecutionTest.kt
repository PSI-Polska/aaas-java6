package pl.psi.aaas.usecase

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.kotlintest.specs.StringSpec

val ValidDefinition = CalculationDefinition(listOf(1), listOf(101), "validScriptPath")


class ScriptExecutionTest : StringSpec() {
    init {
        val NoSynchronizationSynchronizer = mock<ScriptSynchronizer> {
            on { isUnderSynchronization() } doReturn false
        }

        "Validation" {
            TODO("Validation cases")
        }

        "ScriptExecutioner checks with Synchronizer if it can run" {

            val out = JustScriptExecution(NoSynchronizationSynchronizer)

            out.call(ValidDefinition)

            verify(NoSynchronizationSynchronizer, times(1)).isUnderSynchronization()
        }

        "ScriptsExecutioner waits until synchronization is finished" {
            TODO("Synchronization cases")
        }


    }
}