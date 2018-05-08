package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.Query
import pl.psi.aaas.ValuesRepository
import pl.psi.aaas.usecase.CalculationException
import java.time.ZonedDateTime

typealias TsId = Long
typealias TS = Array<Pair<ZonedDateTime, Double?>>

/**
 * Mapped time series values with Symbol.
 */
//typealias MappedTS = Map<Symbol, TS>
class MappedTS : HashMap<Symbol, TS>() {
    private val DT_NAME = "DateTime"

    fun getDateTime(): TS = this[DT_NAME] ?: throw CalculationException("Vector '$DT_NAME' was not found")
    fun allButDT(): MappedTS = filterKeys { it != DT_NAME } as MappedTS
}

/**
 * Time Series [Query] DTO.
 *
 * @param tsId identifies time series to read/save values on
 * @param begin first date used to read/save values array
 * @param end end date used to read/save values array
 */
data class TSQuery(val tsId: TsId, val begin: ZonedDateTime, val end: ZonedDateTime) : Query

/**
 * TimeSeries data repository used by TimeSeries based [CalculationExecution] implementations.
 */
interface TSRepository : ValuesRepository<TSQuery, TS, TS>

// TODO 05.05.2018 kskitek: maybe also provide TSRepo : ValuesRepository<Map<Symbol, TSQuery>, MappedTS, MappedTS>