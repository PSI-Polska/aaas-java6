package pl.psi.aaas.usecase.parameters

import pl.psi.aaas.usecase.Symbol
import java.time.ZonedDateTime

// TODO make this Iterable< ??? >
sealed class Parameter<T : Any>(open var value: T, open val clazz: Class<T>) {
    companion object {
        @JvmStatic
        val supportedClasses: List<Class<*>> = listOf(String::class.java,
                java.lang.Long::class.java, Long::class.java,
                java.lang.Double::class.java, Double::class.java,
                java.lang.Boolean::class.java, Boolean::class.java,
                ZonedDateTime::class.java)

        @JvmStatic
        fun <T : Any> ofPrimitive(value: T): Primitive<T> =
                with(value.javaClass) {
                    when (isSupported(this)) {
                        true -> Primitive(value, this)
                        else -> throw IllegalArgumentException("Unsupported type: ${this}")
                    }
                }

        @JvmStatic
        fun <T : Any> ofArray(value: Array<T?>, elemClazz: Class<T>): Vector<T> =
                when (isSupported(elemClazz)) {
                    true -> Vector(value, value.javaClass, elemClazz)
                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
                }

//        @JvmStatic
//        fun <T : Any> ofArray(value: Array<T?>, elemClazz: Class<T>): Vector<T> =
//                ofArray()

        @JvmStatic
        fun <T : Any> ofArrayNotNull(value: Array<T>, elemClazz: Class<T>): Vector<T> =
                ofArray(value as Array<T?>, elemClazz)

        @JvmStatic
        fun <T : Any> ofArrayNotNull(value: Array<T>): Vector<T> =
                ofArrayNotNull(value, value[0].javaClass)

        @JvmStatic
        fun ofDataFrame(value: Array<Column>): DataFrame {
            val unsupported = value.map { it.vector.elemClazz }.filterNot { isSupported(it) }
            return when (unsupported.size) {
                0 -> when (value.map { it.vector.value.size }.distinct().size) {
                    1 -> DataFrame(value)
                    else -> throw IllegalArgumentException("")
                }
                else -> {
                    val notSupportedClasses = unsupported.joinToString()
                    throw IllegalArgumentException("Unsupported types: $notSupportedClasses")
                }
            }
        }

        @JvmStatic
        private fun isSupported(clazz: Class<*>): Boolean = supportedClasses.contains(clazz)
    }
}

data class Primitive<T : Any> internal constructor(override var value: T, override val clazz: Class<T>)
    : Parameter<T>(value, clazz) {
}

data class Vector<T : Any> internal constructor(override var value: Array<T?>, override val clazz: Class<Array<T?>>, val elemClazz: Class<*>)
    : Parameter<Array<T?>>(value, clazz)

//typealias Column = Pair<Symbol, Vector<Any>>

data class Column(val symbol: Symbol, val vector: Vector<in Any>)

// TODO impl me!!
//data class Matrix<T : Any> internal constructor(override var value: Array<Array<T?>>, override val clazz: Class<Array<Array<T?>>>, val elemClazz: Class<T>)
//    : Parameter<Array<Array<T?>>>(value, clazz)

data class DataFrame internal constructor(override var value: Array<Column>, override val clazz: Class<Array<Column>>)
    : Parameter<Array<Column>>(value, clazz) {

    internal constructor(value: Array<Column>) : this(value, clazz())

    companion object {
        private fun clazz(): Class<Array<Column>> {
            val vector = ofArray(arrayOf<Boolean?>(), Boolean::class.java)
            return arrayOf(Column("A", vector as Vector<Any>)).javaClass
        }
    }
}

