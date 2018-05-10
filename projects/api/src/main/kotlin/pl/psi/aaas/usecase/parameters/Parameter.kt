package pl.psi.aaas.usecase.parameters

import java.time.ZonedDateTime

sealed class Parameter<T : Any> private constructor(open var value: T, open val clazz: Class<T>) {
    companion object {
        @JvmStatic
        val supportedClasses: List<Class<*>> = listOf(String::class.java, ZonedDateTime::class.java)

        @JvmStatic
        fun <T : Any> of(value: T): Primitive<T> =
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


//        @JvmStatic
//        operator fun <T : Collection<V>, reified V> invoke(value: T, collectionClazz: Class<T>, elemClazz: Class<V>): Parameter<Vector<V>> =
//                when (isSupported(elemClazz)) {
//                    true -> Parameter(value.toTypedArray(), collectionClazz)
//                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
//                }

        @JvmStatic
        private fun isSupported(clazz: Class<*>): Boolean = supportedClasses.contains(clazz)
    }
}

data class Primitive<T : Any>(override var value: T, override val clazz: Class<T>)
    : Parameter<T>(value, clazz)

data class Vector<T : Any>(override var value: Array<T?>, override val clazz: Class<Array<T?>>, val elemClazz: Class<*>)
    : Parameter<Array<T?>>(value, clazz)

//data class Matrix() TODO

