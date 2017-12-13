package pl.psi.aaas.usecase

internal class NoSynchronizationSynchronizer : ScriptSynchronizer {
    override fun isUnderSynchronization(): Boolean = false
}