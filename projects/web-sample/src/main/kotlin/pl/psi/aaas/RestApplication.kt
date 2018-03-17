package pl.psi.aaas

import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@ApplicationPath("/")
class RestApplication : Application() {
//    override fun getClasses() = mutableSetOf(RestEndpoint::class.java, RestSynchronizer::class.java)
}