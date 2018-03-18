package pl.psi.aaas

import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.usecase.ScriptSynchronizer
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/update")
class RestSynchronizer @Inject constructor(val connection: RConnectionProvider) : ScriptSynchronizer {
    private var isUnderSynchronization = false

    @GET
    fun synchronize() {
        isUnderSynchronization = true
        startSynchronization()
    }

    override fun isUnderSynchronization() = isUnderSynchronization

    override fun waitEnd() {

    }

    fun startSynchronization() {

    }
}