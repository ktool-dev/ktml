package dev.ktml.parser

import dev.ktml.parser.kotlin.KotlinExpression
import dev.ktml.util.CONTEXT_PARAM_PREFIX
import dev.ktool.gen.safe

/**
 * Represents a template parameter extracted from HTML attributes
 */
data class ParsedTemplateParameter(
    val name: String,
    val expression: KotlinExpression,
) {
    val type = expression.content.substringBefore("=").trim()
    val defaultValue = expression.content.substringAfter("=", "").trim().ifEmpty { null }
    val isContextParam = name.startsWith(CONTEXT_PARAM_PREFIX)
    val isContent = type == "Content" || type == "Content?"
    val hasDefault = defaultValue != null
    val isNullable = type.endsWith("?")
    private val paramName = name.removePrefix(CONTEXT_PARAM_PREFIX)
    private val paramNameSafe = paramName.safe

    fun contextParameterDefinition() = when {
        hasDefault && isNullable -> "val $paramName: $type = optionalNullable(\"$paramName\", ${defaultValue()})"
        hasDefault -> "val $paramNameSafe: $type = optional(\"$paramName\", ${defaultValue()})"
        isNullable -> "val $paramNameSafe: $type = requiredNullable(\"$paramName\")"
        else -> "val $paramNameSafe: $type = required(\"$paramName\")"
    }

    fun contextParameterCall() = when {
        hasDefault && isNullable -> "$paramNameSafe = optionalNullable(\"$paramName\", ${defaultValue()})"
        hasDefault -> "$paramNameSafe = optional(\"$paramName\", ${defaultValue()})"
        isNullable -> "$paramNameSafe = requiredNullable(\"$paramName\")"
        else -> "$paramNameSafe = required(\"$paramName\")"
    }

    private fun defaultValue(): String? = when {
        hasDefault && defaultValue == "null" -> "null as $type"
        else -> defaultValue
    }
}
