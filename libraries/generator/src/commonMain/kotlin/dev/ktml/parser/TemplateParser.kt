package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.ktml.parser.kotlin.EXPRESSION_REPLACE_REGEX
import dev.ktml.parser.kotlin.KotlinExpression
import dev.ktml.parser.kotlin.findByKey
import dev.ktml.parser.kotlin.replaceKotlinExpressions
import dev.ktml.util.CONTEXT_PARAM_PREFIX
import dev.ktml.util.isHtmlElement
import dev.ktml.util.isSvgElement
import dev.ktool.gen.LINE_SEPARATOR

private const val FRAGMENT_INDICATOR = "fragment"
private val FIRST_TAG_REGEX = """(?m)^\s*<""".toRegex()

/**
 * Main template parser that uses Ksoup to parse HTML templates
 */
class TemplateParser(private val moduleName: String = "") {
    private val parserOptions = KsoupHtmlOptions.Default.copy(lowerCaseAttributeNames = false)

    /**
     * Parse template content
     */
    fun parseContent(fileName: String, rawContent: String, subPath: String = moduleName): List<ParsedTemplate> {
        val (replacedContent, expressions) = rawContent.replaceKotlinExpressions()

        val (doctype, content) = replacedContent.checkForDoctype()

        val contentStart = FIRST_TAG_REGEX.find(content)?.range?.start ?: 0

        val headerLines = rawContent.take(contentStart).split(LINE_SEPARATOR).size

        val (imports, externalScriptContent) = parseHeader(fileName, content.substring(0, contentStart))

        val (normalizedContent, selfClosingTag) = content.substring(contentStart).findSelfClosingTags()

        val handler = HtmlHandler(selfClosingTag)

        KsoupHtmlParser(handler = handler, options = parserOptions).apply {
            write(normalizedContent)
            end()
        }

        val rootElements = handler.rootElements

        if (rootElements.any { it.name == "html" }) {
            require(rootElements.size == 1) { "The file $fileName has two html roots in it, you can only have a single html root in a file." }

            val rootElement = rootElements.first()
            val (contextParams, attrs) = rootElement.attrs.entries.partition { it.key.startsWith(CONTEXT_PARAM_PREFIX) }
            val filteredElement = rootElement.copy(attrs = attrs.associate { it.key to it.value })

            return ParsedTemplate(
                file = fileName,
                name = fileName,
                isPage = true,
                inRegistry = true,
                subPath = subPath,
                parameters = extractParameters(contextParams.associate { it.key to it.value }, expressions),
                imports = imports,
                root = HtmlTag("index", mapOf(), mutableListOf(filteredElement)),
                dockTypeDeclaration = doctype,
                expressions = expressions,
                headerLines = headerLines,
                externalScriptContent = externalScriptContent,
            ).let(::listOf)
        }

        rootElements.forEach {
            require(!it.name.isHtmlElement()) { "The tag ${it.name} in $fileName is an existing HTML tag, so you can't use it as a custom tag name." }
            require(!it.name.isSvgElement()) { "The tag ${it.name} in $fileName is an existing SVG tag, so you can't use it as a custom tag name." }
        }

        return rootElements.map {
            ParsedTemplate(
                file = fileName,
                name = it.name,
                subPath = subPath,
                inRegistry = it.attrs.any { (key, value) -> key == FRAGMENT_INDICATOR && value != "false" },
                parameters = extractParameters(it.attrs, expressions),
                imports = imports,
                root = it,
                expressions = expressions,
                headerLines = headerLines,
                externalScriptContent = externalScriptContent,
            )
        }
    }

    private fun String.checkForDoctype(): Pair<String, String> {
        val start = indexOf("<!doctype", ignoreCase = true)

        if (start == -1) return "<!DOCTYPE html>" to this

        val end = indexOf(">", start)
        return substring(start, end + 1) to substring(end + 1)
    }

    private fun parseHeader(fileName: String, content: String): Pair<List<String>, String> {
        if (content.isBlank()) return listOf<String>() to ""

        require(content.lines().none { it.trim().startsWith("package ") }) {
            "You cannot declare a package for your ktml file: $fileName"
        }
        val imports = content.lines().filter { it.trim().startsWith("import ") }
        val script = if (imports.isEmpty()) content.trim() else content.substringAfterLast(imports.last(), "").trim()
        return imports to script
    }

    /**
     * Since Ksoup only handles known HTML self-closing tags, we have to find any other tags that are self-closing and
     * add them to the list of self-closing tags, so they are parsed correctly.
     */
    private val selfClosingRegex = """<(\w+(?:-\w+)*)[^>]*\s*/>""".toRegex()

    private fun String.findSelfClosingTags(): Pair<String, List<String>> {
        val selfClosingTags = selfClosingRegex.findAll(this)
            .map { it.groupValues[1] }
            .toMutableSet()
        var normalized = this

        // If some tags are used as self-closing in one place and not another, we need to normalize the usage to always
        // have a close tag. Otherwise, the HtmlHandler won't handel them all correctly.
        selfClosingTags.toList().forEach { tagName ->
            // Check if the tag also has closing tag usage (with any content between tags)
            if (Regex("""<$tagName[^>]*>[\s\S]*?</$tagName>""").containsMatchIn(this)) {
                // Replace self-closing with explicit closing tags
                normalized = normalized.replace("""<($tagName)([^>]*)\s*/>""".toRegex(), "<$1$2></$1>")
                selfClosingTags.remove(tagName)
            }
        }

        return Pair(normalized, selfClosingTags.map { it.lowercase() }.toSet().toList())
    }

    private fun extractParameters(attrs: Map<String, String>, expressions: List<KotlinExpression>) =
        attrs.filter { it.key != FRAGMENT_INDICATOR }.map { (name, value) ->
            require(EXPRESSION_REPLACE_REGEX.matches(value)) {
                "The template parameter value for $name must be a kotlin expression, so it must start with a $"
            }
            ParsedTemplateParameter(name, expressions.findByKey(value))
        }.sortedWith(
            compareBy(
                { it.isContent }, // Non-Content (false) comes before Content (true)
                { it.isContent && it.name == "content" }, // Content "content" parameter goes last
                { it.name } // Alphabetical within each group
            ))

}
