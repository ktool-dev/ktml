package dev.ktml.parser

import dev.ktml.TagDefinition
import dev.ktml.parser.kotlin.EXPRESSION_REPLACE_REGEX
import dev.ktml.parser.kotlin.KotlinExpression
import dev.ktml.util.ROOT_PACKAGE
import dev.ktml.util.replaceTicks
import dev.ktml.util.toCamelCase
import dev.ktml.util.toPascalCase
import dev.ktool.gen.TRIPLE_QUOTE

/**
 * Represents a parsed template with its metadata
 */
class ParsedTemplate(
    val file: String,
    val name: String,
    val isPage: Boolean = false,
    val inRegistry: Boolean = false,
    val subPath: String = "",
    val imports: List<String>,
    val parameters: List<ParsedTemplateParameter>,
    val expressions: List<KotlinExpression>,
    val root: HtmlTag,
    val dockTypeDeclaration: String = "",
    val externalScriptContent: String = "",
) {
    val path = if (subPath.isNotEmpty()) "$subPath/$name" else name
    val camelCaseName = name.toCamelCase()
    val pathCamelCaseName = if (path.isNotEmpty()) "$subPath/$camelCaseName" else camelCaseName
    val nonContextParameters = parameters.filterNot { it.isContextParam }
    val functionName = "write${name.toCamelCase()}"
    val packageName = if (subPath.isEmpty()) ROOT_PACKAGE else ROOT_PACKAGE + "." +
            subPath.split("/").joinToString(".") { it.toPascalCase() }
    val qualifiedFunctionName = "$packageName.$functionName"
    val uniqueFunctionName = "write" + subPath.replace("/", "-").toCamelCase() + functionName.substringAfter("write")

    fun samePath(template: TagDefinition) = subPath == template.subPath

    fun sameTemplate(template: TagDefinition) = template.uniqueFunctionName == uniqueFunctionName

    fun findExpression(value: String) = value.trim().let { v -> expressions.find { it.key == v || it.uuid == v } }

    fun extractExpressions(value: String): List<ExpressionPart> {
        val parts = mutableListOf<ExpressionPart>()
        var lastIndex = 0

        EXPRESSION_REPLACE_REGEX.findAll(value).forEach { match ->
            // Add text before the match (if any)
            if (match.range.first > lastIndex) {
                parts.add(ExpressionPart(text = value.substring(lastIndex, match.range.first)))
            }

            // Add the matched expression
            parts.add(ExpressionPart(expression = findExpression(match.value)))

            lastIndex = match.range.last + 1
        }

        if (lastIndex < value.length) {
            parts.add(ExpressionPart(text = value.substring(lastIndex)))
        }
        return parts.toList()
    }
}

data class ExpressionPart(val text: String? = null, val expression: KotlinExpression? = null)

fun List<ExpressionPart>.buildString() = StringBuilder().appendExpressions(this).toString()

fun StringBuilder.appendExpressions(expressions: List<ExpressionPart>) {
    append(TRIPLE_QUOTE)
    expressions.forEach {
        if (it.text != null) {
            append(it.text)
        } else if (it.expression != null) {
            append($$"${")
            append(it.expression.kotlinFileContent.replaceTicks())
            append("}")
        }
    }
    append(TRIPLE_QUOTE)
    append(',')
}
