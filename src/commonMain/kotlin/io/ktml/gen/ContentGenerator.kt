package io.ktml.gen

import io.ktml.parser.HtmlElement

/**
 * Generates HtmlWriter method calls from parsed HTML elements
 */
class ContentGenerator {
    fun generateChildrenContent(children: List<HtmlElement>): String = buildString {
        children.forEach { append(generateElementContent(it)) }
    }

    private fun generateElementContent(element: HtmlElement) = when (element) {
        is HtmlElement.Tag -> generateTagContent(element)
        is HtmlElement.Text -> generateTextContent(element)
    }

    private fun generateTagContent(tag: HtmlElement.Tag): String = buildString {
        if (isCustomTemplateTag(tag.name)) {
            return generateCustomTagContent(tag)
        }

        append("raw(\"<${tag.name}")

        tag.attrs.forEach { (name, value) ->
            if (isKotlinValue(value)) {
                append(" $name=\\\"\").text(${extractVariableExpression(value)}).raw(\"\\\"")
            } else {
                append(" $name=\\\"$value\\\"")
            }
        }

        append(">\")")

        append(generateChildrenContent(tag.children))

        if (!isSelfClosingTag(tag.name)) {
            append("raw(\"</${tag.name}>\")")
        }
    }

    /**
     * Generate content for text nodes
     */
    private fun generateTextContent(text: HtmlElement.Text): String {
        val content = text.content.trim()
        if (content.isEmpty()) return ""

        return "text(\"${escapeKotlinString(content)}\")"
    }

    /**
     * Generate content for variable expressions
     */
    private fun generateVariableContent(variable: HtmlElement.Text): String {
        return "text(${variable})"
    }

    /**
     * Generate content for custom template tags
     */
    private fun generateCustomTagContent(tag: HtmlElement.Tag) = buildString {
        val functionName = "render${tag.name.toCamelCase()}"

        append("$functionName(")

        // Add parameters from attributes
        tag.attrs.forEach { (name, value) ->
            if (!endsWith("(")) {
                append(", ")
            }
            if (isKotlinValue(value)) {
                val expression = extractVariableExpression(value)
                append("$name = $expression")
            } else {
                append("$name = \"${escapeKotlinString(value)}\"")
            }
        }

        // Add content parameters from child elements
        val contentParams = extractContentParameters(tag.children)
        contentParams.forEach { (paramName, elements) ->
            append(", $paramName = { ")
            val contentCode = generateChildrenContent(elements)
            if (contentCode.isNotEmpty()) {
                append(" $contentCode ")
            }
            append("}")
        }

        append(")")
    }

    /**
     * Extract content parameters from child elements
     */
    private fun extractContentParameters(children: List<HtmlElement>): Map<String, List<HtmlElement>> {
        val contentParams = mutableMapOf<String, List<HtmlElement>>()

        children.forEach { child ->
            if (child is HtmlElement.Tag && isContentParameterTag(child.name)) {
                contentParams[child.name] = child.children
            }
        }

        return contentParams
    }

    /**
     * Check if a tag name represents a custom template
     */
    private fun isCustomTemplateTag(tagName: String): Boolean {
        return false
    }

    /**
     * Check if a tag name represents a content parameter
     */
    private fun isContentParameterTag(tagName: String): Boolean {
        return tagName in setOf("header", "body", "content")
    }

    private fun isKotlinValue(value: String) = value.startsWith($$"${") && value.endsWith("}")

    /**
     * Extract variable expression from ${...} syntax
     */
    private fun extractVariableExpression(value: String) = value.removeSurrounding($$"${", "}")

    /**
     * Check if a tag is self-closing
     */
    private fun isSelfClosingTag(tagName: String): Boolean {
        return tagName in setOf(
            "br",
            "hr",
            "img",
            "input",
            "meta",
            "link",
            "area",
            "base",
            "col",
            "embed",
            "source",
            "track",
            "wbr"
        )
    }

    /**
     * Escape content for Kotlin string literals
     */
    protected fun escapeKotlinString(content: String): String {
        return content.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Convert kebab-case to CamelCase
     */
    private fun String.toCamelCase(): String {
        return split("-").joinToString("") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}