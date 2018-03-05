package pl.psi.aaas.engine

import pl.psi.aaas.usecase.ScriptSynchronizer

/**
 * This synchronizer does no synchronization at all.
 */
class NoSynchronizationSynchronizer : ScriptSynchronizer {
    override fun waitEnd() {
    }

    override fun isUnderSynchronization(): Boolean = false
}
