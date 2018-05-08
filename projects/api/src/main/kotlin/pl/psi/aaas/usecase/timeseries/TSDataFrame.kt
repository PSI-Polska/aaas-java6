package pl.psi.aaas.usecase.timeseries

import pl.psi.aaas.usecase.Column
import pl.psi.aaas.usecase.DataFrame

class TSDataFrame(columns: Array<String>, matrix: Array<Column<Double?>>) : DataFrame<Double?>(columns, matrix) {

    companion object {
        const val COL_DT = "DateTime"
    }

}

fun Column<Double?>.toDoubleArray(nullReplacement: Double): DoubleArray =
        map { it ?: nullReplacement }.toDoubleArray()
