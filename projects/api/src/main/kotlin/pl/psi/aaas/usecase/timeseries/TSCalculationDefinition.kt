package pl.psi.aaas.usecase.timeseries

import org.joda.time.DateTime
import org.joda.time.Duration
import pl.psi.aaas.usecase.*


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
    val begin: DateTime
    val end: DateTime
    val resolution: Duration
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
 * @property inParameters TODO
 */
data class TSCalcDef(override val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                     override val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                     override val begin: DateTime,
                     override val end: DateTime,
                     override val calculationScript: String,
                     override val inParameters: Parameters = mutableMapOf(),
                     override val outParameters: OutParameters = mutableMapOf(),
                     override val resolution: Duration = Duration.standardHours(1)) : TSCalculationDefinition

data class TSCalcDefWithValues(override val timeSeriesIdsIn: Map<Symbol, Long>,
                               override val timeSeriesIdsOut: Map<Symbol, Long>,
                               override val begin: DateTime,
                               override val end: DateTime,
                               override val calculationScript: String,
                               override val inParameters: Parameters,
                               override val outParameters: OutParameters,
                               override val values: TSDataFrame,
                               override val resolution: Duration = Duration.standardHours(1))
    : TSCalculationDefinition, CalculationDefinitonWithValues<TSDataFrame> {

    constructor(def: TSCalculationDefinition, values: TSDataFrame)
            : this(def.timeSeriesIdsIn, def.timeSeriesIdsOut, def.begin, def.end, def.calculationScript, def.inParameters, def.outParameters, values)
}
