package pl.psi.aaas.usecase

/**
 * The calculation engine implementations are used by use cases implementations ([CalculationExecution]).
 */
interface Engine<in T : CalculationDefinition, out R> {
    /**
     * Call the calculation definition on the engine.
     *
     * @param calcDef TimeSeriesWithValuesCalculationDefinition passed to the engine
     *
     * @throws CalculationException
     */
    @Throws(CalculationException::class)
    fun call(calcDef: T): R
}
