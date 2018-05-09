package pl.psi.aaas.usecase.parameters

import java.time.ZonedDateTime

class Parameter<T : Any> private constructor(var value: T, val clazz: Class<T>, val elemClazz: Class<*>? = null, val primitive: Boolean = true) {
    companion object {
        @JvmStatic
        val supportedClasses: List<Class<*>> = listOf(String::class.java, ZonedDateTime::class.java)

        @JvmStatic
        operator fun <T : Any> invoke(value: T): Parameter<T> =
                with(value.javaClass) {
                    isSupported(this)
                    Parameter(value, this)
                }

        @JvmStatic
        operator fun <T : Any> invoke(value: Array<T>, arrayClazz: Class<Array<T>>, elemClazz: Class<T>): Parameter<Array<T>> =
                when (isSupported(elemClazz)) {
                    true -> Parameter(value, arrayClazz, elemClazz, false)
                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
                }

//        @JvmStatic
//        operator fun <T : Collection<V>, reified V> invoke(value: T, collectionClazz: Class<T>, elemClazz: Class<V>): Parameter<Array<V>> =
//                when (isSupported(elemClazz)) {
//                    true -> Parameter(value.toTypedArray(), collectionClazz)
//                    else -> throw IllegalArgumentException("Unsupported type: ${elemClazz.canonicalName}")
//                }

        @JvmStatic
        private fun isSupported(clazz: Class<*>): Boolean = supportedClasses.contains(clazz)
    }
}

//private data class StringParam(override val name: String, override var value: String) : Parameter<String>(name, value, String::class.java)
//
//private data class LongParam(override val name: String, override var value: Long) : Parameter<Long>(name, value, Long::class.java)
//
//data class DoubleParam(override val name: String, override var value: Double) : Parameter<Double>(name, value, Double::class.java)
//
//data class DateTimeParam(override val name: String, override var value: ZonedDateTime) : Parameter<ZonedDateTime>(name, value, ZonedDateTime::class.java)
//
//data class BooleanParam(override val name: String, override var value: Boolean) : Parameter<Boolean>(name, value, Boolean::class.java)
//
//data class ArrayParam<T>(override val name: String, override var value: Array<Any?>, val arrayClazz: Class<*>)
//    : Parameter<Array<Any?>>(name, value, Array<Any?>::class.java)
//
//fun c() {
//    val java = Array<Any?>::class.java
