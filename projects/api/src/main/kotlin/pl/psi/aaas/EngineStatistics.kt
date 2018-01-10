package pl.psi.aaas

import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import pl.psi.aaas.usecase.CalculationDefinition
import java.util.concurrent.atomic.AtomicLong

@Aspect
object EngineStatistics {
    private val currentlyExecuting = mutableMapOf<CalculationDefinition, AtomicLong>().withDefault { AtomicLong(1) }

    @Before("execution(+ Engine.call(CalculationDefinition) ) ", argNames = "calcDef")
    fun beforeCall(calcDef: CalculationDefinition) {
        currentlyExecuting[calcDef]!!.incrementAndGet()
    }

    @After("")
    fun afterCall(calcDef: CalculationDefinition) {
        currentlyExecuting[calcDef]!!.decrementAndGet()
    }
}