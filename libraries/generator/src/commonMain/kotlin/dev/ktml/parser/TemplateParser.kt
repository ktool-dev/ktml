package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser

private const val FRAGMENT_INDICATOR = "fragment"

/**
 * Main template parser that uses Ksoup to parse HTML templates
 */
class TemplateParser(private val moduleName: String = "") {
    private val parserOptions = KsoupHtmlOptions.Default.copy(lowerCaseAttributeNames = false)

    /**
     * Parse template content
     */
    fun parseContent(fileName: String, rawContent: String, subPath: String = moduleName): List<ParsedTemplate> {
        val (doctype, content) = checkForDoctype(rawContent)

        val imports = parseImportStatements(content)

        val handler = HtmlHandler(findSelfClosingTags(content))

        KsoupHtmlParser(handler = handler, options = parserOptions).apply {
            write(content)
            end()
        }

        val rootElements = handler.rootElements

        if (rootElements.any { it.name == "html" }) {
            if (rootElements.size != 1) error("The file $fileName has two html roots in it, you can only have a single html root in a file.")

            val rootElement = rootElements.first()
            val contextParams = rootElement.attrs.filter { (key, _) -> key.startsWith("ctx-") }
            val filteredElement =
                rootElement.copy(attrs = rootElement.attrs.filter { (key, _) -> !key.startsWith("ctx-") })

            return ParsedTemplate(
                file = fileName,
                name = fileName,
                isPage = true,
                inRegistry = true,
                subPath = subPath,
                parameters = extractParameters(contextParams),
                imports = imports,
                root = HtmlElement.Tag("index", mapOf(), mutableListOf(filteredElement)),
                dockTypeDeclaration = doctype,
                externalScriptContent = handler.externalScriptContent,
            ).let(::listOf)
        }

        return rootElements.map {
            ParsedTemplate(
                file = fileName,
                name = it.name,
                subPath = subPath,
                inRegistry = it.attrs.any { (key, value) -> key == FRAGMENT_INDICATOR && value != "false" },
                parameters = extractParameters(it.attrs),
                imports = imports,
                root = it,
                externalScriptContent = handler.externalScriptContent,
            )
        }
    }

    private fun checkForDoctype(content: String): Pair<String, String> {
        val start = content.indexOf("<!doctype", ignoreCase = true)

        if (start == -1) return "<!DOCTYPE html>" to content

        val end = content.indexOf(">", start)
        return content.substring(start, end + 1) to content.substring(end + 1)
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

    private fun extractParameters(attrs: Map<String, String>) =
        attrs.filter { it.key != FRAGMENT_INDICATOR }.map { (name, typeSpec) ->
            val parts = typeSpec.split("=", limit = 2)
            val defaultValue = if (parts.size > 1) parts[1].trim() else null

            ParsedTemplateParameter(name, parts[0].trim(), defaultValue)
        }.sortedWith(
            compareBy(
                { it.isContent }, // Non-Content (false) comes before Content (true)
                { it.isContent && it.name == "content" }, // Content "content" parameter goes last
                { it.name } // Alphabetical within each group
            ))
}
