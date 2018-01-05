package pl.psi.aaas.usecase

import java.time.ZonedDateTime

/**
 * Time series Symbol.
 */
typealias Symbol = String
/**
 * Parameters of calculation.
 */
typealias Parameters = Map<String, String>

/**
 * DTO to move calculation definition information.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property calculationScriptPath not empty path to calculation R script
 */
// TODO this definition is meant only for Time Series. Split it or rename it!
// TODO open?
data class CalculationDefinition(val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                                 val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                                 val begin: ZonedDateTime,
                                 val end: ZonedDateTime,
                                 val calculationScriptPath: String,
                                 val additionalParameters: Parameters = emptyMap())
// TODO add dataSource?
// TODO add engineDefinition?
// TODO add useCase?

/**
 * CalculationExecution interface describes calculation use cases.
 */
interface CalculationExecution {
    /**
     * Call the calculation.
     *
     * @throws CalculationException
     */
    @Throws(CalculationException::class)
    fun call(calcDef: CalculationDefinition)
}

/**
 * Calculation Exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
open class CalculationException(override val message: String, override val cause: Throwable? = null) : RuntimeException(message, cause)

