package pl.psi.aaas.usecase

internal class MockTimeSeriesRepository : TimeSeriesRepository {
    private val ts = (1 until 24 * 356).toList().map { it.toDouble() }.toDoubleArray()

    override fun read(tsId: TsId): TS = ts

    override fun save(id: TsId, tsValues: TS) {
        println("SAVING")
        println("\tid = $id")
        println("\tsize = ${tsValues.size}")
    }

}
