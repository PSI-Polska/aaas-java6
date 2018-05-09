package pl.psi.aaas.usecase.parameters

import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass

sealed class Parameter<T : Any>(open val name: String, open var value: T)

data class StringParam(override val name: String, override var value: String) : Parameter<String>(name, value)

data class NumberParam(override val name: String, override var value: Number) : Parameter<Number>(name, value)

data class LongParam(override val name: String, override var value: Long) : Parameter<Long>(name, value)

data class DoubleParam(override val name: String, override var value: Double) : Parameter<Double>(name, value)

data class DateTimeParam(override val name: String, override var value: ZonedDateTime) : Parameter<ZonedDateTime>(name, value)

data class BooleanParam(override val name: String, override var value: Boolean) : Parameter<Boolean>(name, value)

data class ArrayParam<T : Parameter<R>, R : Any>(override val name: String, override var value: Array<T?>, val clazz: KClass<T>) : Parameter<Array<T?>>(name, value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrayParam<*, *>

        if (!Arrays.equals(value, other.value)) return false
        if (clazz != other.clazz) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(value)
        result = 31 * result + clazz.hashCode()
        return result
    }
}
