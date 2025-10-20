package dev.ktml.gen

import com.mohamedrejeb.ksoup.entities.KsoupEntities.decodeHtml

fun String.replaceTicks() = replace("\\'", "\u0000").replace("'", "\"").replace("\u0000", "'")

private val interpolationRegex = """\$(?:\{|[_a-zA-Z][_a-zA-Z0-9]*)""".toRegex()
fun String.hasKotlinInterpolation() = interpolationRegex.containsMatchIn(this)

fun String.isSingleKotlinExpression() = trim().run { lastIndexOf($$"${") == 0 && endsWith("}") }

fun String.extractSingleKotlinExpression() = trim().substringAfter($$"${").substringBefore("}")

fun String.extractAttributeExpression() =
    if (isSingleKotlinExpression()) {
        extractSingleKotlinExpression().replaceTicks()
    } else {
        this
    }.let { decodeHtml(it) }

data class Part(val text: String, val isKotlin: Boolean)

fun String.extractMultipleExpressions(): List<Part> {
    val parts = mutableListOf<Part>()
    processTextWithKotlin(this, parts)
    parts.reverse()
    return parts
}

private fun processTextWithKotlin(text: String, parts: MutableList<Part>) {
    if (text.isEmpty()) return

    if (!text.contains($$"${")) {
        parts.add(Part(text, false))
    } else {
        val after = text.substringAfterLast("}")
        if (after.isNotEmpty()) {
            parts.add(Part(after, false))
        }
        val kotlin = decodeHtml(text.substringAfterLast($$"${").substringBeforeLast("}"))
        parts.add(Part(kotlin, true))
        processTextWithKotlin(text.substringBeforeLast($$"${"), parts)
    }
}