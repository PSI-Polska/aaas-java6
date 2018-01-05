package pl.psi.aaas.engine

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.rosuda.REngine.REXPList
import org.rosuda.REngine.RList
import org.rosuda.REngine.Rserve.RConnection

class MockRConnectionProvider : RConnectionProvider {
    private val conn = mock<RConnection>
    {
        on { eval("dfOut <- run(dfIn)") } doReturn REXPList(getResult())
    }

    override fun getConnection(): RConnection = conn

    private fun getResult(): RList {
        val ret = RList()
        ret.put("C", listOf(1))
        return ret
    }
}