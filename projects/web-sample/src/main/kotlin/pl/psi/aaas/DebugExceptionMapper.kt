package pl.psi.aaas

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class DebugExceptionMapper : ExceptionMapper<Throwable> {
    override fun toResponse(exc: Throwable?): Response =
            Response.ok(exc.toString()).status(Response.Status.INTERNAL_SERVER_ERROR).build()
}