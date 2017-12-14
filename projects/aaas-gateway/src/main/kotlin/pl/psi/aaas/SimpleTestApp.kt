package pl.psi.aaas

import pl.psi.aaas.engine.RServeEngine
import pl.psi.aaas.usecase.CalculationDefinition
import java.util.logging.Level
import java.util.logging.Logger

object SimpleTestApp {
    @JvmStatic
    fun main(args: Array<String>) {
        Logger.getLogger(RServeEngine::javaClass.name).level = Level.ALL

        val facade: Facade = FixedFacade

        facade.callScript(prepCalcDef1())
    }

    fun prepCalcDef1(): CalculationDefinition {
        val inIds = mapOf("A" to 1L, "B" to 2L)
        val outIds = mapOf("C" to 3L)

        return CalculationDefinition(inIds, outIds, "/var/userScripts/add.R")
    }
}