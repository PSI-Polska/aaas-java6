package pl.psi.aaas.usecase

/**
 * Parameters of calculation.
 */
typealias Parameters = Map<String, String>

/**
 * The most basic calculation definition.
 */
interface CalculationDefinition {
    val calculationScriptPath: String
    val additionalParameters: Parameters
// TODO add engineDefinition?
}
// TODO add useCase?

/**
 * CalculationExecution interface describes calculation use cases.
 */
interface CalculationExecution<in T : CalculationDefinition> {
    /**
     * Call the calculation.
     *
     * @throws CalculationException
     */
    @Throws(CalculationException::class)
    fun call(calcDef: T)
}

/**
 * Calculation Exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
open class CalculationException(override val message: String, override val cause: Throwable? = null) : RuntimeException(message, cause)

