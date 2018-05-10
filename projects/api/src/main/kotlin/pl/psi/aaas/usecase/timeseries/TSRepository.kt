package pl.psi.aaas.usecase.timeseries

import org.joda.time.DateTime
import org.joda.time.Duration
import pl.psi.aaas.Query
import pl.psi.aaas.ValuesRepository
import pl.psi.aaas.usecase.Symbol

typealias TsId = Long
typealias TSResolution = Duration
typealias TS = Array<Double?>
typealias TSWithResolution = Triple<DateTime, TSResolution, TS>

/**
 * Mapped time series values with Symbol.
 */
typealias MappedTS = Map<Symbol, TSWithResolution>

/**
 * Time Series [Query] DTO.
 *
 * @param tsId identifies time series to read/save values on
 * @param begin first date used to read/save values array
 * @param end end date used to read/save values array
 */
data class TSQuery(val tsId: TsId, val begin: DateTime, val end: DateTime) : Query

/**
 * TimeSeries data repository used by TimeSeries based [CalculationExecution] implementations.
 */
interface TSRepository : ValuesRepository<TSQuery, TS, TS>

// TODO 05.05.2018 kskitek: maybe also provide TSRepo : ValuesRepository<Map<Symbol, TSQuery>, MappedTS, MappedTS>