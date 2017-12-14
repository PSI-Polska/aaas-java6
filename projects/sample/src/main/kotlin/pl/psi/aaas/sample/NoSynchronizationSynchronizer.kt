package pl.psi.aaas.sample

import pl.psi.aaas.usecase.ScriptSynchronizer

internal class NoSynchronizationSynchronizer : ScriptSynchronizer {
    override fun isUnderSynchronization(): Boolean = false
}