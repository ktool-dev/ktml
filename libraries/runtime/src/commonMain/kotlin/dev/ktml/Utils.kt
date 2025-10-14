package dev.ktml

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal fun encodeHtml(value: String?) = if (value.isNullOrBlank()) {
    value
} else {
    value.map {
        when (it) {
            '\'' -> "&apos;"
            '"' -> "&quot;"
            '<' -> "&lt;"
            '>' -> "&gt;"
            '&' -> "&amp;"
            else -> it
        }
    }.joinToString("")
}

@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> String?.toType(): T? = this?.let {
    when (T::class) {
        String::class -> this as T
        Int::class -> toInt() as T
        Long::class -> toLong() as T
        Float::class -> toFloat() as T
        Double::class -> toDouble() as T
        Boolean::class -> toBoolean() as T
        Uuid::class -> Uuid.parse(this) as T
        else -> error("No conversion for type: ${T::class}")
    }
}
