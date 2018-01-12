package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationException
import java.time.ZonedDateTime

typealias TsId = Long
typealias TS = DoubleArray

/**
 * TimeSeries data repository used by TimeSeries based [CalculationExecution] implementations.
 */
interface TimeSeriesRepository {
    /**
     * Read time series.
     *
     * @param tsId time series Id to read
     * @param begin date and time of time series begin
     * @param end date and time of time series end
     *
     * @return time series array
     */
    @Throws(TimeSeriesAccessException::class)
    fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS

    /**
     * Save time series data.
     *
     * @param tsId identifies time series to save values on
     * @param begin first date used to save values array
     * @param tsValues time series values array
     */
    @Throws(TimeSeriesAccessException::class)
    fun save(tsId: TsId, begin: ZonedDateTime, tsValues: TS)
}

/**
 * TimeSeries access operations exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
class TimeSeriesAccessException(override val message: String, override val cause: Throwable? = null) : CalculationException(message, cause)
