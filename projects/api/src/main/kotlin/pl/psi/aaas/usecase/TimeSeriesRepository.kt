package pl.psi.aaas.usecase

import java.time.ZonedDateTime

typealias TsId = Long
typealias TS = DoubleArray

interface TimeSeriesRepository {
    fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS
    fun save(id: TsId, tsValues: TS)
}