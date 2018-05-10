package pl.psi.aaas.sample

import org.joda.time.Interval
import org.slf4j.LoggerFactory
import pl.psi.aaas.usecase.timeseries.TS
import pl.psi.aaas.usecase.timeseries.TSQuery
import pl.psi.aaas.usecase.timeseries.TSRepository

internal class MockTimeSeriesRepository : TSRepository {

    private val log = LoggerFactory.getLogger(MockTimeSeriesRepository::class.java)

    override fun read(query: TSQuery): TS =
            with(Interval(query.begin, query.end).toDuration().standardHours) {
                return (0 until this).map { it.toDouble() }.toTypedArray()
            }

    override fun save(query: TSQuery, values: TS) {
        log.info("SAVING")
        log.info("\ttsId = ${query.tsId}")
        log.info("\tsize = ${values.size}")
    }

}
