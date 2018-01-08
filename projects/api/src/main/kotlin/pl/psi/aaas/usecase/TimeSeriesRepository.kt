package pl.psi.aaas.usecase

import java.time.ZonedDateTime

typealias TsId = Long
typealias TS = Array<Double?>

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
     * @return time series array // TODO nullable
     */
    @Throws(TimeSeriesAccessException::class)
    fun read(tsId: TsId, begin: ZonedDateTime, end: ZonedDateTime): TS

    /**
     * Save time series data.
     *
     * @param tsId identifies time series to save values on
     * @param tsValues time series values array // TODO nullable
     */
    @Throws(TimeSeriesAccessException::class)
    fun save(tsId: TsId, tsValues: TS) // TODO, begin: ZonedDateTime)
}

/**
 * TimeSeries access operations exception.
 *
 * @property message mandatory not empty message
 * @property cause optional cause
 */
class TimeSeriesAccessException(override val message: String, override val cause: Throwable? = null) : CalculationException(message, cause)
