package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.CalculationException
import pl.psi.aaas.usecase.Column
import pl.psi.aaas.usecase.DataFrame

class TSDataFrame(columns: Array<String>, matrix: Array<Column<Double?>>) : DataFrame<Double?>(columns, matrix) {

    companion object {
        const val COL_DT = "DateTime"

        operator fun invoke(ts: MappedTS): TSDataFrame {
//            val columns = ts.keys.plus(COL_DT).toTypedArray()
            val columns = mutableListOf<Symbol>()
            val maxColSize = ts.map { it.value.third.size }.max() ?: 0
//            val matrix = Array(ts.size, { Column<Double?>(maxColSize + 1, { null }) })
            val matrix = arrayListOf<Column<Double?>>()

            columns.add(COL_DT)
            matrix.add()

            // TODO 08.05.2018 kskitek: impl
            return TSDataFrame(columns.toTypedArray(), matrix.toTypedArray())
        }
    }

    fun toMappedTS(): MappedTS = throw CalculationException("Not implemented yet!")

    private fun
}

fun Column<Double?>.toDoubleArray(nullReplacement: Double): DoubleArray =
        map { it ?: nullReplacement }.toDoubleArray()
