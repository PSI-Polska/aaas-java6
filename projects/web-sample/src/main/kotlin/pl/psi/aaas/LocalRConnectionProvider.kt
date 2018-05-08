package pl.psi.aaas

import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration
import javax.ejb.Stateless

@Stateless
class LocalRConnectionProvider(
        override var configuration: REngineConfiguration = REngineConfiguration("engine", 6311)
) : RConnectionProvider
