package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.Column
import pl.psi.aaas.usecase.DataFrame
import pl.psi.aaas.usecase.Symbol
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

// TODO maybe it is good to use only one DataFrame or MappedTS?

// TODO TSDataFrame and generaly DataFrame should be a pl.psi.aaas.inParameters.Parameter<??>
class TSDataFrame(columns: Array<String>, matrix: Array<Column<Double?>>) : DataFrame<Double?>(columns, matrix) {

    companion object {
        const val COL_DT = "DateTime"

        operator fun invoke(ts: MappedTS): TSDataFrame {
            if (ts.isEmpty())
                return TSDataFrame(emptyArray(), emptyArray())

            ts.validateMappedTS()

            val columns = mutableListOf<Symbol>()
            val matrix = arrayListOf<Column<Double?>>()

            addDateTimeColumn(ts, columns, matrix)
            addValuesColumns(ts, columns, matrix)

            return TSDataFrame(columns.toTypedArray(), matrix.toTypedArray())
        }

        private fun addValuesColumns(ts: MappedTS, columns: MutableList<Symbol>, matrix: ArrayList<Column<Double?>>) {
            ts.forEach {
                columns.add(it.key)
                matrix.add(it.value.third)
            }
        }

        private fun addDateTimeColumn(ts: MappedTS, columns: MutableList<Symbol>, matrix: ArrayList<Column<Double?>>) {
            val (zonedDateTime, period, values) = ts[ts.keys.first()]!!
            columns.add(COL_DT)
            matrix.add(createDTVector(zonedDateTime, period, values.size))
        }

        private fun MappedTS.validateMappedTS() {
            val beginsAndResolutions = map { it.value.first to it.value.second }.distinct()
            if (beginsAndResolutions.size > 1)
                throw IllegalArgumentException("TSDataFrame requires one begin DateTime and one raster. Got: ${beginsAndResolutions.joinToString()}")
            val valuesSizes = map { it.value.third.size }.distinct()
            if (valuesSizes.size > 1)
                throw IllegalArgumentException("TSDataFrame requires all value arrays to be equal size. Got: ${valuesSizes.joinToString()}")
        }

        private fun createDTVector(beginDateTime: ZonedDateTime, period: TSResolution, size: Int): Column<Double?> {
            val retCol = Column<Double?>(size, { null })
            var currLen = 0
            var currDate = beginDateTime
            while (currLen < size) {
                retCol[currLen] = currDate.toEpochSecond().toDouble()
                currLen++
                currDate = currDate.plus(period)
            }
            return retCol
        }
    }

    fun getDateTime(): Column<ZonedDateTime>? =
            get(COL_DT)?.map { it?.toLong() ?: 0 }
                    ?.map { Instant.ofEpochSecond(it) }
                    ?.map { ZonedDateTime.ofInstant(it, ZoneOffset.UTC) }?.toTypedArray()
}

fun Column<Double?>.toDoubleArray(nullReplacement: Double): DoubleArray =
        map { it ?: nullReplacement }.toDoubleArray()
