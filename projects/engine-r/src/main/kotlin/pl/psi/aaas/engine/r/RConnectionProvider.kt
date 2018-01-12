package pl.psi.aaas.engine.r

import org.rosuda.REngine.Rserve.RConnection

data class REngineConfiguration(val address: String, val port: Int)

interface RConnectionProvider {
    val configuration: REngineConfiguration
    fun getConnection(): RConnection = RConnection(configuration.address, configuration.port)
}
