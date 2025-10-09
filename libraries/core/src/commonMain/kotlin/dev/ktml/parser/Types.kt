package dev.ktml.parser

import dev.ktml.TagDefinition
import dev.ktml.util.ROOT_PACKAGE
import dev.ktml.util.toCamelCase
import dev.ktml.util.toPascalCase
import dev.ktool.gen.safe

/**
 * Represents a parsed template with its metadata
 */
data class ParsedTemplate(
    val file: String,
    val name: String,
    val isPage: Boolean = false,
    val subPath: String = "",
    val imports: List<String>,
    val parameters: List<ParsedTemplateParameter>,
    val root: HtmlElement.Tag,
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
}

/**
 * Represents a template parameter extracted from HTML attributes
 */
data class ParsedTemplateParameter(
    val name: String,
    val type: String,
    val defaultValue: String? = null
) {
    val isContextParam = name.startsWith("ctx-")
    val isContent = type == "Content" || type == "Content?"
    val hasDefault = defaultValue != null
    val isNullable = type.endsWith("?")
    private val paramName = name.removePrefix("ctx-")
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

/**
 * Represents an HTML element in the parsed template
 */
sealed class HtmlElement {
    data class Tag(
        val name: String,
        val attrs: Map<String, String>,
        private val _children: MutableList<HtmlElement> = mutableListOf()
    ) : HtmlElement() {
        val children: List<HtmlElement> get() = _children.toList()
        val isKotlinScript = name == "script" && attrs["type"] == "text/kotlin"

        fun addChild(element: HtmlElement) {
            _children.add(element)
        }
    }

    data class Text(var content: String) : HtmlElement()
}

fun List<HtmlElement>.removeEmptyText() = filterNot { it is HtmlElement.Text && it.content.isBlank() }
