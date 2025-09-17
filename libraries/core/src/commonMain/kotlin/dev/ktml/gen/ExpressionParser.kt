package dev.ktml.gen

import com.mohamedrejeb.ksoup.entities.KsoupEntities.decodeHtml

class ExpressionParser {
    fun isKotlinExpression(value: String) = value.startsWith($$"${") && value.endsWith("}")

    fun hasKotlinExpression(value: String) = value.contains($$"${") && value.contains("}")

    data class Part(val text: String, val isKotlin: Boolean)

    fun extractMultipleExpressions(content: String): List<Part> {
        val parts = mutableListOf<Part>()
        processTextWithKotlin(content, parts)
        parts.reverse()
        return parts
    }

    private fun processTextWithKotlin(text: String, parts: MutableList<Part>) {
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

    fun extractSingleExpression(value: String) = decodeHtml(value.removeSurrounding($$"${", "}"))
}