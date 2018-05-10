package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationDefinitonWithValues
import pl.psi.aaas.usecase.Parameters
import pl.psi.aaas.usecase.Symbol
import java.time.ZonedDateTime


/**
 * Definition used to move TimeSeries calculation definition information. Used by [pl.psi.aaas.usecase.CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date ofPrimitive Time Series
 * @property end end date ofPrimitive Time Series
 */
interface TSCalculationDefinition : CalculationDefinition {
    val timeSeriesIdsIn: Map<Symbol, Long>
    val timeSeriesIdsOut: Map<Symbol, Long>
    val begin: ZonedDateTime
    val end: ZonedDateTime
}
// TODO add dataSource based interface?

/**
 * DTO used to move TimeSeries calculation definition information. Used by [pl.psi.aaas.usecase.CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date ofPrimitive Time Series
 * @property end end date ofPrimitive Time Series
 * @property calculationScript TODO
 * @property parameters TODO
 */
data class TSCalcDef(override val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                     override val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                     override val begin: ZonedDateTime,
                     override val end: ZonedDateTime,
                     override val calculationScript: String,
                     override val parameters: Parameters = emptyMap()) : TSCalculationDefinition

data class TSCalcDefWithValues(override val timeSeriesIdsIn: Map<Symbol, Long>,
                               override val timeSeriesIdsOut: Map<Symbol, Long>,
                               override val begin: ZonedDateTime,
                               override val end: ZonedDateTime,
                               override val calculationScript: String,
                               override val parameters: Parameters,
                               override val values: TSDataFrame)
    : TSCalculationDefinition, CalculationDefinitonWithValues<TSDataFrame> {

    constructor(def: TSCalculationDefinition, values: TSDataFrame)
            : this(def.timeSeriesIdsIn, def.timeSeriesIdsOut, def.begin, def.end, def.calculationScript, def.parameters, values)
}
