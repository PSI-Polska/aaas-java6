package pl.psi.aaas.usecase

typealias TsId = Long
typealias TS = DoubleArray

interface TimeSeriesRepository {
    fun read(tsId: TsId): TS
    fun save(id: TsId, tsValues: TS)
}