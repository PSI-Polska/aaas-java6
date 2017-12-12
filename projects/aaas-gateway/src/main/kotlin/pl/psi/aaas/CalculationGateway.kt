package pl.psi.aaas

import pl.psi.aaas.usecase.CalculationDefinition


/**
 * Calculation endpoint.
 */
interface CalculationGateway {

    /**
     * Schedules execution of given definition. Execution is asynchronous.
     *
     * @param aDefinition calculation definition to be executed
     * @return
     */
    fun callCalculation(aDefinition: CalculationDefinition)

    /**
     * Schedules execution of given definition and waits for calculation end.
     *
     * @param aDefinition calculation definition to be executed
     */
    fun callCalculationSync(aDefinition: CalculationDefinition)
}

/**
 * Calculation Exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
class CalculationException(override val message: String, override val cause: Throwable? = null) : RuntimeException(message)
