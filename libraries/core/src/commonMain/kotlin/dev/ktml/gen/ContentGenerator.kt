package dev.ktml.gen

import dev.ktml.Templates
import dev.ktml.escapeIfKeyword
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.removeEmptyText
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class TemplateContent(val imports: List<String>, val functionContent: String, val rawConstants: List<RawConstant>)

/**
 * Generates HtmlWriter method calls from parsed HTML elements
 */
class ContentGenerator(private val templates: Templates) {
    private val contentBuilder = ContentBuilder()
    private val expressionParser = ExpressionParser()
    private val imports = mutableListOf<String>()
    private lateinit var rootTemplate: ParsedTemplate

    fun generateTemplateContent(template: ParsedTemplate): TemplateContent {
        logger.debug { "Generating content for template: ${template.name}" }
        rootTemplate = template
        contentBuilder.clear()

        initializeImports(template)
        generateContextParams(template)

        if (template.dockTypeDeclaration.isNotBlank()) {
            contentBuilder.doctype(template.dockTypeDeclaration)
        }

        generateChildContent(template, template.root.children)

        val contentAndConstants = contentBuilder.templateContent
        logger.debug { "Finished generating content for template: ${template.name}" }

        return TemplateContent(
            imports = imports.sorted(),
            functionContent = contentAndConstants.content,
            rawConstants = contentAndConstants.rawConstants,
        )
    }

    private fun initializeImports(template: ParsedTemplate) {
        imports.clear()
        imports.addAll(template.imports)

        if (template.parameters.any { it.type == "Content" }) {
            imports.add("import dev.ktml.Content")
        }

        imports.add("import dev.ktml.Context")
    }

    private fun generateContextParams(template: ParsedTemplate) {
        template.parameters.filter { it.isContextParam }.forEach { param ->
            val name = param.name.removePrefix("ctx-")
            logger.debug { "Generating context param: $name" }
            val defaultValue =
                if (param.type == "String" && param.defaultValue != null) "\"${param.defaultValue}\"" else param.defaultValue

            when {
                param.defaultValue != null && param.type.endsWith("?") ->
                    contentBuilder.kotlin("val $name: ${param.type} = optionalNullable(\"$name\") ?: $defaultValue")

                param.defaultValue != null ->
                    contentBuilder.kotlin("val $name: ${param.type} = optional(\"$name\", $defaultValue)")

                param.type.endsWith("?") ->
                    contentBuilder.kotlin("val $name: ${param.type} = requiredNullable(\"$name\")")

                else ->
                    contentBuilder.kotlin("val $name: ${param.type} = required(\"$name\")")
            }
        }
    }

    private fun generateChildContent(
        template: ParsedTemplate,
        children: List<HtmlElement>,
        noInterpolation: Boolean = false
    ) {
        children.forEach {
            when (it) {
                is HtmlElement.Tag -> generateTagContent(template, it, noInterpolation)
                is HtmlElement.Text -> generateTextContent(it, noInterpolation)
            }
        }
    }

    private val controlAttrs = setOf("if", "each")
    private val filteredAttrs = controlAttrs + "ignore-kotlin"

    private fun generateTagContent(template: ParsedTemplate, tag: HtmlElement.Tag, noInterpolation: Boolean = false) {
        logger.debug { "Generating tag content: ${tag.name}" }

        val customTag = templates.locate(tag.name, template)

        // This prevents a template from calling itself
        if (customTag != null && customTag != rootTemplate) return generateCustomTagCall(template, tag, customTag)

        if (tag.isKotlinScript) {
            tag.children.joinToString("") { (it as HtmlElement.Text).content.trim() }
                .split("\n").also { contentBuilder.kotlin(it) }

            return
        }

        tag.attrs["if"]?.let {
            contentBuilder.startControlFlow("if", expressionParser.extractSingleExpression(it))
        }

        tag.attrs["each"]?.let {
            contentBuilder.startControlFlow("for", expressionParser.extractSingleExpression(it))
        }

        val currentNoInterpolation = noInterpolation || tag.attrs.containsKey("ignore-kotlin")

        contentBuilder.raw("<${tag.name}")

        tag.attrs.filterNot { it.key in filteredAttrs }.forEach { (name, value) ->
            if (!currentNoInterpolation && expressionParser.isKotlinExpression(value)) {
                contentBuilder.raw(" $name=\"")
                contentBuilder.write(expressionParser.extractSingleExpression(value))
                contentBuilder.raw("\"")
            } else {
                contentBuilder.raw(" $name=\"$value\"")
            }
        }

        contentBuilder.raw(">")

        generateChildContent(template, tag.children, currentNoInterpolation)

        if (tag.children.isNotEmpty()) {
            contentBuilder.raw("</${tag.name}>")
        } else if (!SELF_CLOSING_TAGS.contains(tag.name)) {
            contentBuilder.raw(" />")
        }

        tag.attrs.filter { it.key in controlAttrs }.forEach { _ ->
            contentBuilder.endControlFlow()
        }
    }

    private fun generateTextContent(text: HtmlElement.Text, noInterpolation: Boolean = false) {
        logger.debug { "Generating text content: ${text.content}" }
        val content = text.content
        if (!noInterpolation && expressionParser.hasKotlinExpression(content)) {
            expressionParser.extractMultipleExpressions(content).forEach { part ->
                if (part.isKotlin) {
                    contentBuilder.write(part.text)
                } else {
                    contentBuilder.raw(part.text)
                }
            }
        } else {
            contentBuilder.raw(content)
        }
    }

    private fun generateCustomTagCall(template: ParsedTemplate, tag: HtmlElement.Tag, customTag: ParsedTemplate) {
        logger.info { "Generating custom tag call: ${customTag.name}" }

        if (!template.samePackage(customTag)) {
            imports.add("import ${customTag.packageName}.${customTag.functionName}")
        }

        contentBuilder.startTemplateCall(customTag.functionName)

        val templateParams = customTag.orderedParameters.filterNot { it.isContextParam }
        val remainingChildren = tag.children.toMutableList()
        val lastParam = templateParams.lastOrNull()

        if (templateParams.isEmpty()) {
            contentBuilder.deleteLastNewLine()
            contentBuilder.endTemplateCall()
            return
        } else if (templateParams.size == 1 && lastParam?.isContent == true) {
            contentBuilder.deleteLastNewLine()
        }

        templateParams.forEach { param ->
            val paramName = escapeIfKeyword(param.name)

            if (param.isContent) {
                val child = tag.children.filterIsInstance<HtmlElement.Tag>().find { it.name == param.name }

                val content = when {
                    child != null -> {
                        remainingChildren.remove(child)
                        child.children
                    }

                    param.name == "content" -> remainingChildren.removeEmptyText()
                    else -> null
                }

                if (content.isNullOrEmpty()) {
                    if (param.defaultValue != "null") {
                        error("Missing required content for parameter '${param.name}' for tag '${customTag.name}'")
                    }
                } else if (param == lastParam) {
                    contentBuilder.endTemplateCallWithContent()
                    contentBuilder.startEmbeddedContent()
                    println("Content: $content")
                    generateChildContent(customTag, content)
                    contentBuilder.endEmbeddedContent()
                } else {
                    println("Content: $content")
                    contentBuilder.startEmbeddedContent("$paramName = ")
                    generateChildContent(customTag, content)
                    contentBuilder.endEmbeddedContent(",")
                }
            } else {
                val value = if (!tag.attrs.containsKey(param.name)) {
                    if (param.hasDefault) {
                        param.defaultValue
                    } else {
                        error("Missing required attribute '${param.name}' for tag '${customTag.name}'")
                    }
                } else {
                    tag.attrs[param.name]
                }

                if (!param.isNullable && (value == null || value == "null")) {
                    error("Null value passed for non-nullable attribute '${param.name}' for tag '${customTag.name}'")
                }

                when {
                    value == null -> contentBuilder.kotlin("$paramName = null,")
                    expressionParser.isKotlinExpression(value) ->
                        contentBuilder.kotlin("$paramName = ${expressionParser.extractSingleExpression(value)},")

                    param.type == "String" -> contentBuilder.kotlin("${param.name} = \"$value\",")
                    else -> contentBuilder.kotlin("$paramName = $value,")
                }
            }
        }

        if (lastParam?.isContent != true) {
            contentBuilder.endTemplateCall()
        }
    }
}

private val SELF_CLOSING_TAGS = setOf(
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
    "wbr",
)