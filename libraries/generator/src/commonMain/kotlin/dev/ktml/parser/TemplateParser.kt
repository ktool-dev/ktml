package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.ktml.util.isHtmlElement
import dev.ktml.util.isSvgElement

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

        val (normalizedContent, selfClosingTag) = findSelfClosingTags(content)

        val handler = HtmlHandler(selfClosingTag)

        KsoupHtmlParser(handler = handler, options = parserOptions).apply {
            write(normalizedContent)
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
                parameters = extractParameters(fileName, contextParams),
                imports = imports,
                root = HtmlElement.Tag("index", mapOf(), mutableListOf(filteredElement)),
                dockTypeDeclaration = doctype,
                externalScriptContent = handler.externalScriptContent,
            ).let(::listOf)
        }

        rootElements.forEach {
            if (it.name.isHtmlElement()) error("The tag ${it.name} is an existing HTML tag, so you can't use it as a custom tag name.")
            if (it.name.isSvgElement()) error("The tag ${it.name} is an existing SVG tag, so you can't use it as a custom tag name.")
        }

        return rootElements.map {
            ParsedTemplate(
                file = fileName,
                name = it.name,
                subPath = subPath,
                inRegistry = it.attrs.any { (key, value) -> key == FRAGMENT_INDICATOR && value != "false" },
                parameters = extractParameters(it.name, it.attrs),
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
    private val selfClosingRegex = """<(\w+(?:-\w+)*)[^>]*\s*/>""".toRegex()

    private fun findSelfClosingTags(content: String): Pair<String, List<String>> {
        val selfClosingTags = selfClosingRegex.findAll(content)
            .map { it.groupValues[1] }
            .toMutableSet()
        var normalized = content

        // If some tags are used as self-closing in one place and not another, we need to normalize the usage to always
        // have a close tag.
        selfClosingTags.toList().forEach { tagName ->
            // Check if the tag also has closing tag usage (with any content between tags)
            if (Regex("""<$tagName[^>]*>[\s\S]*?</$tagName>""").containsMatchIn(content)) {
                // Replace self-closing with explicit closing tags
                normalized = normalized.replace("""<($tagName)([^>]*)\s*/>""".toRegex(), "<$1$2></$1>")
                selfClosingTags.remove(tagName)
            }
        }

        return Pair(normalized, selfClosingTags.toList())
    }

    private fun extractParameters(tagName: String, attrs: Map<String, String>): List<ParsedTemplateParameter> =
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
