package pl.psi.aaas.usecase

internal class NoSynchronizationMock : ScriptSynchronizer {
    override fun isUnderSynchronization(): Boolean = false
}