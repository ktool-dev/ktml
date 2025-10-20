package dev.ktml.gen

import com.mohamedrejeb.ksoup.entities.KsoupEntities.decodeHtml

fun String.replaceTicks() = replace("\\'", "\u0000").replace("'", "\"").replace("\u0000", "'")

private val interpolationStartRegex = """\$(?:\{|[`_a-zA-Z][_a-zA-Z0-9]*)""".toRegex()
private val interpolationValueRegex = """^\$(`[^`]+`|[_a-zA-Z][_a-zA-Z0-9]*)""".toRegex()
fun String.hasKotlinInterpolation() = interpolationStartRegex.containsMatchIn(this)

fun String.isSingleKotlinExpression() = trim().run { lastIndexOf($$"${") == 0 && endsWith("}") }

fun String.extractSingleKotlinExpression() = trim().substringAfter($$"${").substringBefore("}")

fun String.extractAttributeExpression() =
    if (isSingleKotlinExpression()) {
        extractSingleKotlinExpression().replaceTicks()
    } else {
        this
    }.let { decodeHtml(it) }

data class Part(val text: String, val isKotlin: Boolean)

fun String.extractExpressions(): List<Part> {
    val parts = mutableListOf<Part>()
    processTextWithKotlin(this, parts)
    return parts.toList()
}

private fun processTextWithKotlin(text: String, parts: MutableList<Part>) {
    var remaining = text

    while (remaining.isNotEmpty()) {
        if (!remaining.contains("$")) {
            parts.add(Part(remaining, false))
            remaining = ""
        } else if (!remaining.startsWith("$")) {
            val index = remaining.indexOf("$")
            parts.add(Part(remaining.take(index), false))
            remaining = remaining.substring(index)
        } else if (remaining.startsWith($$"${")) {
            val start = remaining.indexOf($$"${") + 2
            val end = findMatchingCloseBrace(remaining, start)

            if (end == -1) {
                // If no matching brace found, treat as non-Kotlin text
                parts.add(Part(remaining, false))
                remaining = ""
            } else {

                parts.add(Part(decodeHtml(remaining.substring(start, end).trimIndent()), true))

                remaining = remaining.substring(end + 1)
            }
        } else if (remaining.take(5).hasKotlinInterpolation()) {
            val match = interpolationValueRegex.find(remaining)
            val value = match?.groupValues?.get(1) ?: ""

            if (value.isEmpty()) {
                parts.add(Part("$", false))
            } else {
                parts.add(Part(value, true))
            }
            remaining = remaining.substring(value.length + 1)
        } else {
            parts.add(Part("$", false))
            remaining = remaining.substring(1)
        }
    }
}

private fun findMatchingCloseBrace(text: String, startIndex: Int): Int {
    var braceCount = 1
    var i = startIndex
    var inString = false
    var stringChar = '\u0000'
    var escaped = false

    while (i < text.length && braceCount > 0) {
        val char = text[i]

        if (escaped) {
            escaped = false
            i++
            continue
        }

        when {
            char == '\\' -> escaped = true
            !inString && (char == '"' || char == '\'') -> {
                inString = true
                stringChar = char
            }

            inString && char == stringChar -> {
                inString = false
                stringChar = '\u0000'
            }

            !inString && char == '{' -> braceCount++
            !inString && char == '}' -> braceCount--
        }

        i++
    }

    return if (braceCount == 0) i - 1 else -1
}