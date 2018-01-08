package pl.psi.aaas.sample

import org.slf4j.LoggerFactory
import pl.psi.aaas.usecase.TS
import pl.psi.aaas.usecase.TimeSeriesRepository
import pl.psi.aaas.usecase.TsId
import java.time.ZonedDateTime

internal class MockTimeSeriesRepository : TimeSeriesRepository {
    private val log = LoggerFactory.getLogger(MockTimeSeriesRepository::class.java)

    private val ts = (1 until 24 * 356).toList().map { it.toDouble() }.toDoubleArray()

    override fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS = ts

    override fun save(tsId: TsId, tsValues: TS) {
        log.info("SAVING")
        log.info("\ttsId = $tsId")
        log.info("\tsize = ${tsValues.size}")
    }

}
