package pl.psi.aaas.engine.r.transceiver

import org.rosuda.REngine.REXPDouble
import org.rosuda.REngine.Rserve.RConnection
import pl.psi.aaas.engine.r.RValuesTransceiver
import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.parameters.DateTimeParam
import pl.psi.aaas.usecase.parameters.StringParam

class StringTransceiver<in D : CalculationDefinition>(override val session: RConnection) : RValuesTransceiver<StringParam, StringParam, D> {
    override fun send(value: StringParam, definition: D) {
        session.assign(value.name, value.value)
        session.voidEval("str(S)")
    }

    override fun receive(result: Any?, definition: D): StringParam? {
        TODO("not implemented")
    }
}

class DateTimeTransceiver<in D : CalculationDefinition>(override val session: RConnection) : RValuesTransceiver<DateTimeParam, DateTimeParam, D> {
    override fun send(value: DateTimeParam, definition: D) {
        val epochSecond = value.value.toEpochSecond()
        session.assign(value.name, REXPDouble(epochSecond.toDouble()))
        session.voidEval("${value.name} <- structure(${value.name}, class=c('POSIXt','POSIXct'))")
//        attr(d1, "tzone") <- "UTC" TODO
        session.voidEval(value.name)
    }

    override fun receive(result: Any?, definition: D): DateTimeParam? {
        TODO("not implemented")
    }
}