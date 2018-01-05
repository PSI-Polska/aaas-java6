package pl.psi.aaas.usecase

/**
 * ScriptSynchronizer is responsible for signaling ongoing synchronization of scripts on engine instances
 * to implementations of [CalculationExecution].
 * It is important that no script execution is done during synchronization of scripts on engine instances.
 */
interface ScriptSynchronizer {
    /**
     * Returns true if synchronization is ongoing.
     */
    fun isUnderSynchronization(): Boolean
    /**
     * Waits for synchronization to end.
     */
    fun waitEnd()
}