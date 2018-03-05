package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationDefinition
import pl.psi.aaas.usecase.Parameters
import java.time.ZonedDateTime

/**
 * Time series Symbol.
 */
typealias Symbol = String

/**
 * List of mapped time series values with Symbol.
 */
typealias MappedTS = Collection<Pair<Symbol, TS>>

/**
 * Definition used to move TimeSeries calculation definition information. Used by [CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date of Time Series
 * @property end end date of Time Series
 */
interface TimeSeriesCalculationDefinition : CalculationDefinition {
    val timeSeriesIdsIn: Map<Symbol, Long>
    val timeSeriesIdsOut: Map<Symbol, Long>
    val begin: ZonedDateTime
    val end: ZonedDateTime
}
// TODO add dataSource based interface?

/**
 * DTO used to move TimeSeries calculation definition information. Used by [CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date of Time Series
 * @property end end date of Time Series
 * @property calculationScriptPath TODO
 * @property additionalParameters TODO
 */
data class TSCalcDefDTO(override val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                        override val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                        override val begin: ZonedDateTime,
                        override val end: ZonedDateTime,
                        override val calculationScriptPath: String,
                        override val additionalParameters: Parameters = emptyMap()) : TimeSeriesCalculationDefinition

/**
 * Definition used to move TimeSeries calculation definition information. Used by [CalculationExecution] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date of Time Series
 * @property end end date of Time Series
 */
interface TimeSeriesWithValuesCalculationDefinition : TimeSeriesCalculationDefinition {
    val tsValues: MappedTS
}

/**
 * DTO used to move TimeSeries calculation definition information with values. Used by [Engine] implementations.
 *
 * @property timeSeriesIdsIn Time Series IN identifiers
 * @property timeSeriesIdsOut Time Series OUT identifiers
 * @property begin begin date of Time Series
 * @property end end date of Time Series
 * @property calculationScriptPath TODO
 * @property additionalParameters TODO
 */
data class TSCalcDefWithValuesDTO(override val timeSeriesIdsIn: Map<Symbol, Long> = emptyMap(),
                                  override val timeSeriesIdsOut: Map<Symbol, Long> = emptyMap(),
                                  override val begin: ZonedDateTime,
                                  override val end: ZonedDateTime,
                                  override val calculationScriptPath: String,
                                  override val additionalParameters: Parameters = emptyMap(),
                                  override val tsValues: MappedTS) : TimeSeriesWithValuesCalculationDefinition {
    constructor(dto: TimeSeriesCalculationDefinition, values: MappedTS) :
            this(dto.timeSeriesIdsIn, dto.timeSeriesIdsOut, dto.begin, dto.end, dto.calculationScriptPath, dto.additionalParameters, values)
}
