package dev.ktml

const val DEFAULT_PACKAGE = "dev.ktml.templates"

fun String.encodeHtml() = map {
    when (it) {
        '\'' -> "&apos;"
        '"' -> "&quot;"
        '<' -> "&lt;"
        '>' -> "&gt;"
        '&' -> "&amp;"
        else -> it
    }
}.joinToString("")
