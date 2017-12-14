package pl.psi.aaas.usecase

import org.apache.logging.log4j.LogManager

internal class MockTimeSeriesRepository : TimeSeriesRepository {
    private val log = LogManager.getLogger()

    private val ts = (1 until 24 * 356).toList().map { it.toDouble() }.toDoubleArray()

    override fun read(tsId: TsId): TS = ts

    override fun save(id: TsId, tsValues: TS) {
        log.info("SAVING")
        log.info("\tid = $id")
        log.info("\tsize = ${tsValues.size}")
    }

}
