package pl.psi.aaas.sample

import org.slf4j.LoggerFactory
import pl.psi.aaas.usecase.timeseries.TS
import pl.psi.aaas.usecase.timeseries.TSQuery
import pl.psi.aaas.usecase.timeseries.TSRepository
import java.time.Duration

internal class MockTimeSeriesRepository : TSRepository {

    private val log = LoggerFactory.getLogger(MockTimeSeriesRepository::class.java)

    override fun read(query: TSQuery): TS {
        val hours = Duration.between(query.begin, query.end).toHours()
        return (0 until hours).map { query.begin.plusHours(it) to it.toDouble() }.toTypedArray()
    }

    override fun save(query: TSQuery, values: TS) {
        log.info("SAVING")
        log.info("\ttsId = ${query.tsId}")
        log.info("\tsize = ${values.size}")
    }

}
