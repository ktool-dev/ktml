package dev.ktool.ktml.parser

import dev.ktool.ktml.TEMPLATE_PACKAGE
import dev.ktool.ktml.toCamelCase


/**
 * Represents a parsed template with its metadata
 */
data class ParsedTemplate(
    val name: String,
    val packageName: String = TEMPLATE_PACKAGE,
    val imports: List<String>,
    val parameters: List<TemplateParameter>,
    val root: HtmlElement.Tag,
    val topExternalScriptContent: String = "",
    val bottomExternalScriptContent: String = "",
) {
    val key = "$packageName.$name"
    val camelCaseName = name.toCamelCase()
    val functionName = "write$camelCaseName"
    val qualifiedFunctionName = "$packageName.$functionName"

    val orderedParameters: List<TemplateParameter> by lazy {
        parameters.sortedWith(compareBy({ it.name == "content" }, { it.name }))
    }
}

/**
 * Represents a template parameter extracted from HTML attributes
 */
data class TemplateParameter(
    val name: String,
    val type: String,
    val defaultValue: String? = null
)

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

    data class Text(val content: String) : HtmlElement()
}
