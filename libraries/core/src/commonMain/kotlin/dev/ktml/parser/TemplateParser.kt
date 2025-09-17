package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.ktml.TEMPLATE_PACKAGE

/**
 * Main template parser that uses Ksoup to parse HTML templates
 */
class TemplateParser {
    private val parserOptions = KsoupHtmlOptions.Default.copy(lowerCaseAttributeNames = false)

    /**
     * Parse template content
     */
    fun parseContent(content: String, packageName: String = ""): ParsedTemplate {
        val imports = parseImportStatements(content)

        val handler = HtmlHandler()
        KsoupHtmlParser(handler = handler, options = parserOptions).apply {
            write(content)
            end()
        }

        val rootElements = handler.rootElements

        require(rootElements.size == 1) { "KTML parsing error: Found ${rootElements.size} custom tags in content: \n$content" }

        val rootElement = rootElements.first()

        return ParsedTemplate(
            name = rootElement.name,
            packageName = TEMPLATE_PACKAGE + if (packageName.isNotEmpty()) ".$packageName" else "",
            parameters = extractParameters(rootElement),
            imports = imports,
            root = rootElement,
            topExternalScriptContent = handler.topExternalScriptContent,
            bottomExternalScriptContent = handler.bottomExternalScriptContent,
        )
    }

    private fun parseImportStatements(content: String): List<String> =
        content.substringBefore("<").lines().filter { it.trim().startsWith("import ") }

    private fun extractParameters(rootElement: HtmlElement.Tag) = rootElement.attrs.map { (name, typeSpec) ->
        val parts = typeSpec.split("=", limit = 2)
        val defaultValue = if (parts.size > 1) parts[1].trim().removeSurrounding("'") else null

        TemplateParameter(name, parts[0].trim(), defaultValue)
    }
}
