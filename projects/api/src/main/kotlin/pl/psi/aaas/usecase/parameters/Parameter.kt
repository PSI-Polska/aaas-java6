package pl.psi.aaas.usecase.parameters

import org.joda.time.DateTime
import pl.psi.aaas.usecase.Symbol

/**
 * Parameter class represents all values communicated with the [pl.psi.aaas.Engine] (both ways).
 * Parameter is used by [pl.psi.aaas.EngineValuesSender] and [pl.psi.aaas.EngineValuesReceiver].
 * Parameter has four implementations:
 * * [Primitive]
 * * [Vector]
 * * [Matrix]
 * * [DataFrame]
 *
 * Currently supported types are:
 * * String
 * * Long
 * * Double
 * * Boolean
 * * ZonedDateTime
 */
// TODO make this Iterable< ??? >
sealed class Parameter<T : Any>(open var value: T, open val clazz: Class<T>) {
    companion object {
        @JvmStatic
        val supportedClasses: List<Class<*>> = listOf(String::class.java,
                java.lang.Long::class.java, Long::class.java,
                java.lang.Double::class.java, Double::class.java,
                java.lang.Boolean::class.java, Boolean::class.java,
                DateTime::class.java)

        /**
         * Returns new [Primitive] value.
         * Value cannot be null.
         */
        @JvmStatic
        fun <T : Any> ofPrimitive(value: T): Primitive<T> =
                with(value.javaClass) {
                    when (isSupported(this)) {
                        true -> Primitive(value, this)
                        else -> throw IllegalArgumentException("Unsupported type: ${this}")
                    }
                }

        /**
         * Returns a [Vector] of nullable elements.
         * Array cannot be null.
         *
         * @param T array element type
         * @param value the array
         * @param elemClazz Class of element of an array
         */
        @JvmStatic
        fun <T : Any> ofArray(value: Array<T?>, elemClazz: Class<T>): Vector<T> =
                when (isSupported(elemClazz)) {
                    true -> Vector(value, value.javaClass, elemClazz)
                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
                }

        /**
         * Returns empty [Vector] of nullable elements.
         *
         * @param value the array
         * @param elemClazz Class of element of an array
         */
        @JvmStatic
        inline fun <reified T : Any> ofArray(elemClazz: Class<T>): Vector<T> =
                ofArray(emptyArray<T?>(), elemClazz)

        /**
         * Returns a [Vector] of not null elements.
         * Array cannot be null.
         *
         * @param T array element type
         * @param value the array
         * @param elemClazz Class of element of an array
         */
        @JvmStatic
        fun <T : Any> ofArrayNotNull(value: Array<T>, elemClazz: Class<T>): Vector<T> =
                ofArray(value as Array<T?>, elemClazz)

        /**
         * Returns a [DataFrame] - array of [Column]s.
         * Array cannot be null.
         */
        @JvmStatic
        @Deprecated("There is no point in specifying type two times", replaceWith = ReplaceWith("ofDataFrame(Array<Column>)"))
        fun ofDataFrame(value: Array<Column>, columnClasses: Array<Class<Any>>): DataFrame =
                ofDataFrame(value)

        /**
         * Returns a [DataFrame] - array of [Column]s.
         * Array cannot be null.
         */
        @JvmStatic
        fun ofDataFrame(value: Array<Column>): DataFrame {
            val classes = value.map { it.vector.elemClazz }.toTypedArray() as Array<Class<Any>>
            val unsupported = classes.filterNot { isSupported(it) }
            return when (unsupported.size) {
                0 -> when (value.map { it.vector.value.size }.distinct().size) {
                    1 -> DataFrame(value, classes)
                    else -> throw IllegalArgumentException("")
                }
                else -> {
                    val notSupportedClasses = unsupported.joinToString()
                    throw IllegalArgumentException("Unsupported types: $notSupportedClasses")
                }
            }
        }

        fun emptyVector(elemClazz: Class<out Any>): Vector<Any> =
                ofArrayNotNull(emptyArray(), elemClazz as Class<Any>)

        @JvmStatic
        private fun isSupported(clazz: Class<*>): Boolean = supportedClasses.contains(clazz)
    }
}

/**
 * Primitive value that can be sent and received to the [pl.psi.aaas.Engine].
 *
 * Currently supported types are:
 * * String
 * * Long
 * * Double
 * * Boolean
 * * ZonedDateTime
 */
data class Primitive<T : Any> internal constructor(override var value: T, override val clazz: Class<T>)
    : Parameter<T>(value, clazz)

/**
 * Vector of primitive values.
 * Supports only types supported by [Primitive].
 */
data class Vector<T : Any> internal constructor(override var value: Array<T?> = emptyArray<Any>() as Array<T?>,
                                                override val clazz: Class<Array<T?>>, val elemClazz: Class<*>)
    : Parameter<Array<T?>>(value, clazz)

/**
 * Column of [DataFrame] consists of:
 * @param symbol column name
 * @param vector rows of given column
 */
data class Column(val symbol: Symbol, val vector: Vector<Any>)

// TODO impl me!!
//data class Matrix<T : Any> internal constructor(override var value: Array<Array<T?>>, override val clazz: Class<Array<Array<T?>>>, val elemClazz: Class<T>)
//    : Parameter<Array<Array<T?>>>(value, clazz)

/**
 * Represents DataFrame - array of names, heterogeneous [Vector]s.
 * Only types supported by [Vector] can be used.
 */
data class DataFrame internal constructor(override var value: Array<Column> = emptyArray(),
                                          override val clazz: Class<Array<Column>>,
                                          val columnClasses: Array<Class<Any>>)
    : Parameter<Array<Column>>(value, clazz), Map<String, Vector<Any>> {

    private val internalMap: Map<String, Vector<Any>> by lazy { value.map { it.symbol to it.vector as Vector<Any> }.toMap() }

    override val entries: Set<Map.Entry<String, Vector<Any>>>
        get() = internalMap.entries
    override val keys: Set<String>
        get() = internalMap.keys
    override val size: Int
        get() = internalMap.size
    override val values: Collection<Vector<Any>>
        get() = internalMap.values

    override fun containsKey(key: String): Boolean =
            keys.contains(key)

    override fun containsValue(value: Vector<Any>): Boolean =
            values.contains(value)

    override fun get(key: String): Vector<Any>? =
            internalMap[key]

    override fun isEmpty(): Boolean =
            internalMap.isEmpty()

    fun rowIterator(): Iterator<Array<Any>> =
            DataFrameIterator.of(this)

    internal constructor(value: Array<Column>, columnClasses: Array<Class<Any>>) : this(value, clazz(), columnClasses)

    companion object {
        private fun clazz(): Class<Array<Column>> {
            // just a "hack" to have instance of and array of columns
            val vector = ofArray(arrayOf<Boolean?>(), Boolean::class.java)
            return arrayOf(Column("A", vector as Vector<Any>)).javaClass
        }
    }
}

private class DataFrameIterator(private val iterators: Array<Iterator<Any>>) : Iterator<Array<Any>> {
    companion object {
        @JvmStatic
        fun of(dataFrame: DataFrame): DataFrameIterator {
            val iterators = dataFrame.value.map { it.vector.value.iterator() }.toTypedArray() as Array<Iterator<Any>>
            return DataFrameIterator(iterators)
        }
    }

    override fun hasNext(): Boolean =
            if (iterators.size > 0)
                iterators[0].hasNext()
            else
                false

    override fun next(): Array<Any> =
            if (!hasNext())
                throw IndexOutOfBoundsException()
            else
                iterators.map { it.next() }.toTypedArray()
}

