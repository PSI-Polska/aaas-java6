package pl.psi.aaas.engine

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.REXPList
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RConnectionProvider
import pl.psi.aaas.engine.r.REngineConfiguration

val EmptyConfiguration = REngineConfiguration("", 1)

class MockRConnectionProvider(override var configuration: REngineConfiguration = EmptyConfiguration) : RConnectionProvider {
    private val conn = mock<RConnection>
    {
        on { eval("dfOut <- run(dfIn, inParameters)") } doReturn REXPList(getResult())
    }

    override fun getConnection(): RConnection = conn

    private fun getResult(): RList {
        val ret = RList()
        ret.put("Y", REXPDouble(1.0))
        ret.put("Z", REXPDouble(2.0))
        return ret
    }
}