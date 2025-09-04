package io.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser

/**
 * Represents a parsed template with its metadata
 */
data class ParsedTemplate(
    val name: String,
    val imports: List<String>,
    val parameters: List<TemplateParameter>,
    val root: HtmlElement.Tag,
)

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
    data class Tag(val name: String, val attrs: Map<String, String>, val children: List<HtmlElement>) : HtmlElement()
    data class Text(val content: String) : HtmlElement()
}

/**
 * Main template parser that uses Ksoup to parse HTML templates
 */
class TemplateParser {
    private val parserOptions = KsoupHtmlOptions.Default.copy(lowerCaseAttributeNames = false)

    fun parseTemplate(content: String, rootName: String = ""): ParsedTemplate {
        return parseContent(content, rootName)[0]
    }

    /**
     * Parse template content
     */
    fun parseContent(content: String, rootName: String = ""): List<ParsedTemplate> {
        val imports = content.substringBefore("<").lines().filter { it.trim().startsWith("import ") }

        val handler = TemplateHtmlHandler()
        val parser = KsoupHtmlParser(handler = handler, options = parserOptions)
        parser.write(content)
        parser.end()

        return handler.rootElements.map {
            ParsedTemplate(
                name = rootName + it.name,
                parameters = extractParameters(it),
                imports = imports,
                root = it,
            )
        }
    }

    private fun extractParameters(rootElement: HtmlElement.Tag) = rootElement.attrs.map { (name, typeSpec) ->
        val parts = typeSpec.split("=", limit = 2)
        val defaultValue = if (parts.size > 1) parts[1].trim().removeSurrounding("'") else null

        TemplateParameter(name, parts[0].trim(), defaultValue)
    }
}

/**
 * Custom Ksoup HTML handler for parsing templates
 */
private class TemplateHtmlHandler : KsoupHtmlHandler {
    private val elementStack = mutableListOf<MutableList<HtmlElement>>()
    private var _rootElements = mutableListOf<HtmlElement.Tag>()

    val rootElements: List<HtmlElement.Tag>
        get() = _rootElements

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        println(attributes)
        val children = mutableListOf<HtmlElement>()
        val element = HtmlElement.Tag(name, attributes, children)

        if (elementStack.isEmpty()) {
            _rootElements.add(element)
        } else {
            elementStack.last().add(element)
        }

        if (!isImplied) {
            elementStack.add(children)
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (!isImplied && elementStack.isNotEmpty()) {
            elementStack.removeLastOrNull()
        }
    }

    override fun onText(text: String) {
        if (elementStack.isNotEmpty()) {
            elementStack.last().add(HtmlElement.Text(text))
        }
    }

    override fun onComment(comment: String) {
    }

    override fun onError(error: Exception) {
        throw IllegalArgumentException("HTML parsing error: ${error.message}", error)
    }
}
