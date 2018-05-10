package pl.psi.aaas.usecase.parameters

import pl.psi.aaas.usecase.Symbol
import java.time.ZonedDateTime

sealed class Parameter<T : Any> private constructor(open var value: T, open val clazz: Class<T>) {
    companion object {
        @JvmStatic
        val supportedClasses: List<Class<*>> = listOf(String::class.java, Long::class.java, Double::class.java, Boolean::class.java, ZonedDateTime::class.java)

        @JvmStatic
        fun <T : Any> ofPrimitive(value: T): Primitive<T> =
                with(value.javaClass) {
                    isSupported(this)
                    Primitive(value, this)
                }

        @JvmStatic
        fun <T : Any> of(value: Array<T?>, vectorClazz: Class<Array<T?>>, elemClazz: Class<T>): Vector<T> =
                when (isSupported(elemClazz)) {
                    true -> Vector(value, vectorClazz, elemClazz)
                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
                }

        @JvmStatic
        fun <T : Any> ofNN(value: Array<T>, vectorClazz: Class<Array<T>>, elemClazz: Class<T>): Vector<T> =
                of(value as Array<T?>, vectorClazz as Class<Array<T?>>, elemClazz)

        @JvmStatic
        fun of(value: Array<Column>): DataFrame =
                with(value.map { it.second.elemClazz }.filterNot { isSupported(it) }) {
                    when (size) {
                        0    -> when (value.map { it.second.value.size }.distinct().size) {
                            1    -> DataFrame(value)
                            else -> throw IllegalArgumentException("")
                        }
                        else -> {
                            val notSupportedClasses = joinToString()
                            throw IllegalArgumentException("Unsupported types: $notSupportedClasses")
                        }
                    }
                }

        @JvmStatic
        private fun isSupported(clazz: Class<*>): Boolean = supportedClasses.contains(clazz)
    }
}

data class Primitive<T : Any> internal constructor(override var value: T, override val clazz: Class<T>)
    : Parameter<T>(value, clazz)

data class Vector<T : Any> internal constructor(override var value: Array<T?>, override val clazz: Class<Array<T?>>, val elemClazz: Class<*>)
    : Parameter<Array<T?>>(value, clazz)

typealias Column = Pair<Symbol, Vector<Any>>

data class DataFrame internal constructor(override var value: Array<Column>, override val clazz: Class<Array<Column>>)
    : Parameter<Array<Column>>(value, clazz) {

    constructor(value: Array<Column>) : this(value, clazz())

    companion object {
        fun clazz(): Class<Array<Column>> {
            val vector = of(arrayOf<Boolean?>(), Array<Boolean?>::class.java, Boolean::class.java)
            return arrayOf(Column("A", vector as Vector<Any>)).javaClass
        }
    }
}

//data class Matrix() TODO

