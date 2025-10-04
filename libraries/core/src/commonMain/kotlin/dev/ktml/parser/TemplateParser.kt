package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser

const val DOCTYPE_ERROR_MESSAGE = "<!DOCTYPE> should not be used in templates, use <doctype> tag instead."

/**
 * Main template parser that uses Ksoup to parse HTML templates
 */
class TemplateParser {
    private val parserOptions = KsoupHtmlOptions.Default.copy(lowerCaseAttributeNames = false)

    /**
     * Parse template content
     */
    fun parseContent(content: String, subPath: String = ""): ParsedTemplate {
        val doctype = checkForDoctype(content)

        val imports = parseImportStatements(content)

        val handler = HtmlHandler(findSelfClosingTags(content))

        KsoupHtmlParser(handler = handler, options = parserOptions).apply {
            write(content)
            end()
        }

        val rootElements = handler.rootElements

        require(rootElements.size == 1) { "KTML parsing error: Found ${rootElements.size} custom tags in content: \n$content" }

        val rootElement = rootElements.first()

        return ParsedTemplate(
            name = rootElement.name,
            subPath = subPath,
            parameters = extractParameters(rootElement),
            imports = imports,
            root = rootElement,
            dockTypeDeclaration = doctype,
            externalScriptContent = handler.externalScriptContent,
        )
    }

    private fun checkForDoctype(content: String): String {
        val start = content.indexOf("<!doctype", ignoreCase = true)

        if (start == -1) return ""

        val end = content.indexOf(">", start)
        return content.substring(start, end + 1)
    }

    private fun parseImportStatements(content: String): List<String> =
        content.substringBefore("<").lines().filter { it.trim().startsWith("import ") }

    /**
     * Since Ksoup only handles known HTML self-closing tags, we have to find any other tags that are self-closing and
     * add them to the list of self-closing tags, so they are parsed correctly.
     */
    private fun findSelfClosingTags(content: String): Set<String> {
        val selfClosingRegex = """<(\w+(?:-\w+)*)[^>]*\s*/>""".toRegex()
        return selfClosingRegex.findAll(content)
            .map { it.groupValues[1] }
            .toSet()
    }

    private fun extractParameters(rootElement: HtmlElement.Tag) = rootElement.attrs.map { (name, typeSpec) ->
        val parts = typeSpec.split("=", limit = 2)
        val defaultValue = if (parts.size > 1) parts[1].trim().removeSurrounding("'") else null

        ParsedTemplateParameter(name, parts[0].trim(), defaultValue)
    }.sortedWith(
        compareBy(
            { it.isContent }, // Non-Content (false) comes before Content (true)
            { it.isContent && it.name == "content" }, // Content "content" parameter goes last
            { it.name } // Alphabetical within each group
        ))
}
