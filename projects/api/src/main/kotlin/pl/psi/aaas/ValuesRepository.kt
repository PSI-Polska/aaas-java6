package pl.psi.aaas

import pl.psi.aaas.usecase.CalculationException

/**
 * Used by [ValuesRepository] to read values before script execution.
 *
 * @param Q read Query type parameter
 * @param V read values type parameter
 */
interface ValuesReader<in Q : Query, out V> {
    @Throws(RepositoryAccessException::class)
    fun read(query: Q): V
}

/**
 * Used by [ValuesRepository] to save return values after script execution.
 *
 * @param Q save Query type parameter
 * @param V save values type parameter
 */
interface ValuesWriter<in Q : Query, in V> {
    @Throws(RepositoryAccessException::class)
    fun save(query: Q, values: V)
}

/**
 * Used by [pl.psi.aaas.usecase.CalculationExecution] to read and save values in the product.
 *
 * @param Q read and save Query type parameter
 * @param Vr read values type parameter
 * @param Vw save values type parameter
 */
interface ValuesRepository<in Q : Query, out Vr, in Vw> : ValuesReader<Q, Vr>, ValuesWriter<Q, Vw>

/**
 * Query interface used by [ValuesRepository].
 * Currently it is only a marker interface.
 */
interface Query

/**
 * TimeSeries access operations exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
class RepositoryAccessException(override val message: String, override val cause: Throwable? = null) : CalculationException(message, cause)

/**
 * Adapter class to create [ValuesRepository].
 * It is necessary due to Java does not have class level multiple inheritance.
 *
 * @param Q read and save Query type parameter
 * @param Vr read values type parameter
 * @param Vw save values type parameter
 */
class RW<in Q : Query, out Vr, in Vw>(private val reader: ValuesReader<Q, Vr>,
                                      private val writer: ValuesWriter<Q, Vw>) : ValuesRepository<Q, Vr, Vw> {
    override fun read(query: Q): Vr = reader.read(query)

    override fun save(query: Q, values: Vw) = writer.save(query, values)
}
