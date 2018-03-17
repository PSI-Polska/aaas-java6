package pl.psi.aaas

import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import javax.ejb.Stateless

@Stateless
class LocalRConnectionProvider(
        override var configuration: REngineConfiguration = REngineConfiguration("localhost", 6311)
) : RConnectionProvider
