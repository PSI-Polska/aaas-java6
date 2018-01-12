package pl.psi.aaas.usecase

import pl.psi.aaas.usecase.timeseries.TS

/**
 * List of mapped time series values with Symbol.
 */
typealias MappedTS = List<Pair<Symbol, TS>>

/**
 * The calculation engine implemntations are used by use cases implementations ([CalculationExecution]).
 */
interface Engine {
    // TODO call is meant for time series. Split it into steps (prepare data, call, get data). Generify this class
    /**
     * Call the calculation definition on the engine.
     *
     * @param calcDef CalculationDefinition passed to the engine
     *
     * @throws CalculationException
     */
    @Throws(CalculationException::class)
    fun call(calcDef: CalculationDefinition, tsValues: MappedTS): MappedTS
}
