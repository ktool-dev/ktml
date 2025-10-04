package dev.ktml.parser

import dev.ktml.TemplateDefinition
import dev.ktml.TemplateParameter
import dev.ktml.util.TEMPLATE_PACKAGE
import dev.ktml.util.toCamelCase
import dev.ktml.util.toPascalCase
import dev.ktool.gen.safe


/**
 * Represents a parsed template with its metadata
 */
data class ParsedTemplate(
    val name: String,
    val subPath: String = "",
    val imports: List<String>,
    val parameters: List<ParsedTemplateParameter>,
    val root: HtmlElement.Tag,
    val dockTypeDeclaration: String = "",
    val externalScriptContent: String = "",
) {
    val packageName = if (subPath.isEmpty()) TEMPLATE_PACKAGE else TEMPLATE_PACKAGE + "." +
            subPath.split("/").joinToString(".") { it.replace("_", "-").toPascalCase() }
    val fullPath = if (subPath.isEmpty()) name else "$subPath/$name"

    val isRootPackage = packageName == TEMPLATE_PACKAGE
    private val relativePackage = packageName.substringAfter("$TEMPLATE_PACKAGE.")

    val camelCaseName = name.toCamelCase()
    val functionName = "write$camelCaseName"
    val qualifiedFunctionName = "$packageName.$functionName"
    val uniqueFunctionName = if (isRootPackage) functionName else
        "write" + relativePackage.replace(".", "-").toCamelCase() + camelCaseName

    val isTopLevel = parameters.filterNot { it.isContextParam }.isEmpty()
    val nonContextParameters = parameters.filterNot { it.isContextParam }

    val templateDefinition = TemplateDefinition(
        name = name,
        subPath = subPath,
        packageName = packageName,
        functionName = functionName,
        parameters = nonContextParameters.map { TemplateParameter(it.name, it.type, it.defaultValue != null) }
    )

    fun samePackage(template: TemplateDefinition) = packageName == template.packageName

    fun sameTemplate(template: TemplateDefinition) = qualifiedFunctionName == template.qualifiedFunctionName
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
    val isString = type == "String" || type == "String?"
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
        isString && hasDefault -> "\"$defaultValue}\""
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
