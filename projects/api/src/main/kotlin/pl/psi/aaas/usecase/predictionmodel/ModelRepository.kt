package pl.psi.aaas.usecase.predictionmodel

import pl.psi.aaas.usecase.CalculationException

/**
 * TODO not used anywhere yet!
 */
interface ModelRepository {
    @Throws(ModelAccessException::class)
    fun readModel(path: String)

    @Throws(ModelAccessException::class)
    fun saveModel(path: String)
}

class ModelAccessException(override val message: String, override val cause: Throwable? = null) : CalculationException(message, cause)