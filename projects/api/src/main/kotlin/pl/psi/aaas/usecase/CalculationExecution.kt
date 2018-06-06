package pl.psi.aaas.usecase

import pl.psi.aaas.usecase.parameters.Parameter

typealias Symbol = String
typealias Parameters = Map<Symbol, Parameter<*>>
typealias OutParameters = MutableMap<Symbol, Parameter<*>>

/**
 * The most basic calculation definition.
 */
interface CalculationDefinition {
    val calculationScript: String
    val inParameters: Parameters
    val outParameters: OutParameters
// TODO add engineDefinition? engineQuery?
}

/**
 * Calculation definiton passed with values.
 * Can be used when:
 * - no [pl.psi.aaas.ValuesRepository] is used to read values
 * - [pl.psi.aaas.Engine] implementations have to get definition already with the values
 */
interface CalculationDefinitonWithValues<out V> : CalculationDefinition {
    val values: V
}

/**
 * CalculationExecution interface describes calculation use cases.
 */
@Deprecated("This class is destined for removal in 0.3. All parameters will be sent with the help of Parameter class without need to implement default use cases in the library.", replaceWith = ReplaceWith("In 0.3 There will be no CalculationExecution class."))
interface CalculationExecution<in T : CalculationDefinition, out V> {
    /**
     * Call the calculation.
     *
     * @throws CalculationException
     */
    @Throws(CalculationException::class)
    fun call(calcDef: T): V
}

/**
 * Calculation Exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
open class CalculationException(override val message: String, override val cause: Throwable? = null) : RuntimeException(message, cause)

