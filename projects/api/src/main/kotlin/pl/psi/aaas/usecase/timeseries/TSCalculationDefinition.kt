package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.CalculationDefinitonWithValues
import pl.psi.aaas.usecase.Parameters
import java.time.ZonedDateTime

/**
 * Time series Symbol.
 */
typealias Symbol = String

/**
 * Definition used to move TimeSeries calculation definition information. Used by [pl.psi.aaas.usecase.CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date of Time Series
 * @property end end date of Time Series
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
 * @property begin begin date of Time Series
 * @property end end date of Time Series
 * @property calculationScript TODO
 * @property additionalParameters TODO
 */
data class TSCalcDef(override val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                     override val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                     override val begin: ZonedDateTime,
                     override val end: ZonedDateTime,
                     override val calculationScript: String,
                     override val additionalParameters: Parameters = emptyMap()) : TSCalculationDefinition

data class TSCalcDefWithValues(override val timeSeriesIdsIn: Map<Symbol, Long>,
                               override val timeSeriesIdsOut: Map<Symbol, Long>,
                               override val begin: ZonedDateTime,
                               override val end: ZonedDateTime,
                               override val calculationScript: String,
                               override val additionalParameters: Parameters,
                               override val values: TSDataFrame)
    : TSCalculationDefinition, CalculationDefinitonWithValues<TSDataFrame> {

    constructor(def: TSCalculationDefinition, values: TSDataFrame)
            : this(def.timeSeriesIdsIn, def.timeSeriesIdsOut, def.begin, def.end, def.calculationScript, def.additionalParameters, values)
}
