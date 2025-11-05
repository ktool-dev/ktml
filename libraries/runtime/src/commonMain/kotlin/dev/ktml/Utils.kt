package dev.ktml

const val DEFAULT_PACKAGE = "dev.ktml.templates"

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
