package pl.psi.aaas.sample

import org.apache.logging.log4j.LogManager
import pl.psi.aaas.usecase.TS
import pl.psi.aaas.usecase.TimeSeriesRepository
import pl.psi.aaas.usecase.TsId
import java.time.ZonedDateTime

internal class MockTimeSeriesRepository : TimeSeriesRepository {
    private val log = LogManager.getLogger()

    private val ts = (1 until 24 * 356).toList().map { it.toDouble() }.toDoubleArray()

    override fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS = ts

    override fun save(id: TsId, tsValues: TS) {
        log.info("SAVING")
        log.info("\tid = $id")
        log.info("\tsize = ${tsValues.size}")
    }

}
