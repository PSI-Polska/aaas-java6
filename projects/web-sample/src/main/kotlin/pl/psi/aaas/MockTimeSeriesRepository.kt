package pl.psi.aaas

import org.slf4j.LoggerFactory
import pl.psi.aaas.usecase.timeseries.TS
import pl.psi.aaas.usecase.timeseries.TimeSeriesRepository
import pl.psi.aaas.usecase.timeseries.TsId
import java.time.ZonedDateTime
import javax.ejb.Stateless

@Stateless
class MockTimeSeriesRepository : TimeSeriesRepository {
    private val log = LoggerFactory.getLogger(MockTimeSeriesRepository::class.java)

    private val ts = (1 until 24 * 356).toList().map { it.toDouble() }.toDoubleArray()

    override fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS = ts

    override fun save(tsId: TsId, begin: ZonedDateTime, tsValues: TS) {
        log.info("SAVING")
        log.info("\ttsId = $tsId")
        log.info("\tsize = ${tsValues.size}")
    }

}
