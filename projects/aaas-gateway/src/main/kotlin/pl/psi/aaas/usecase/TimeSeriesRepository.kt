package pl.psi.aaas.usecase

typealias TsId = Long
typealias TS = Array<Long>

interface TimeSeriesRepository {
    fun read(tsId: TsId): TS
}