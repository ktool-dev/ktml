package dev.ktml

import dev.ktml.parser.ParsedTemplateParameter
import dev.ktml.parser.kotlin.KotlinExpression
import dev.ktml.parser.kotlin.Location

fun parsedTemplateParameter(name: String, type: String, default: String? = null) = ParsedTemplateParameter(
    name = name,
    expression = KotlinExpression(if (default == null) type else "$type = $default", Location(0, 0), false),
)

data class User(val name: String)
