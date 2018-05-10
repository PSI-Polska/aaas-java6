package pl.psi.aaas.usecase


/**
 * Very crude implementation ofPrimitive DataFrame.
 * When moving to Java8 think about moving to external implementation like ch.netzwerg.paleo.DataFrame.
 */
open class DataFrame<T>(private val columns: Map<String, Int>, private val matrix: Array<Column<T>>) {
    // TODO 07.05.2018 kskitek: handle different size ofPrimitive Columns in constructor and size()
    // should the Column classes be available at runtime?
    // Maybe make the matrix Array<Column<*>> so we can have heterogeneous DataFrames?
    constructor(columns: Array<String>, matrix: Array<Column<T>>) : this(arrayToMap(columns), matrix)

    fun get(row: Int): Column<T>? =
            if (row < matrix.size)
                matrix[row]
            else
                null

    operator fun get(colName: String): Column<T>? =
            if (columns.contains(colName))
                get(columns[colName]!!)
            else
                null

    fun getAll(): Array<Column<T>> =
            matrix

    fun getColumns(): List<String> =
            columns.entries
                    .sortedBy { it.value }
                    .map { it.key }

    // this would not be very efficient..
//    fun getRow()

//    fun forEachWithColumn(): Stream<Par<String, Column<*>>> =

    fun size(): Int =
            if (matrix.isNotEmpty())
                matrix[0].size
            else
                0


    /**
     * Returns a view ofPrimitive the DataFrame with predicate applied to columns.
     */
    fun getFiltered(predicate: (colName: String) -> Boolean): DataFrame<T> =
            let {
                val filtered = columns.filterKeys(predicate)
                DataFrame(filtered, matrix)
            }

    fun toMap(): Map<String, Column<T>> =
            columns.keys.map { it to get(it)!! }.toMap()
}

typealias Column<T> = Array<T>

private fun arrayToMap(arr: Array<String>): Map<String, Int> =
        arr.mapIndexed { i, s -> s to i }.toMap()