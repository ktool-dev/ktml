package dev.ktml.gen

import dev.ktml.Templates
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class TemplateContent(val functionContent: String, val rawConstants: String)

/**
 * Generates HtmlWriter method calls from parsed HTML elements
 */
class ContentGenerator(private val templates: Templates) {
    private val contentBuilder = ContentBuilder()
    private val expressionParser = ExpressionParser()
    private val _addedImports = mutableListOf<String>()

    val addedImports: List<String> get() = _addedImports.toList()

    fun generateTemplateContent(template: ParsedTemplate): TemplateContent {
        logger.debug { "Generating content for template: ${template.name}" }
        contentBuilder.clear()
        _addedImports.clear()
        generateContextParams(template)
        generateChildContent(template, template.root.children)
        logger.debug { "Finished generating content for template: ${template.name}" }
        return contentBuilder.templateContent
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

    private fun generateChildContent(template: ParsedTemplate, children: List<HtmlElement>) {
        children.forEach {
            when (it) {
                is HtmlElement.Tag -> generateTagContent(template, it)
                is HtmlElement.Text -> generateTextContent(it)
            }
        }
    }

    private val controlAttrs = setOf("if", "each")

    private fun generateTagContent(template: ParsedTemplate, tag: HtmlElement.Tag) {
        logger.info { "Generating tag content: ${tag.name}" }
        val customTag = templates.locate(tag.name, template)

        if (customTag != null) return generateCustomTagCall(template, tag, customTag)

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

        contentBuilder.raw("<${tag.name}")

        tag.attrs.filterNot { it.key in controlAttrs }.forEach { (name, value) ->
            if (expressionParser.isKotlinExpression(value)) {
                contentBuilder.raw(" $name=\"")
                contentBuilder.write(expressionParser.extractSingleExpression(value))
                contentBuilder.raw("\"")
            } else {
                contentBuilder.raw(" $name=\"$value\"")
            }
        }

        contentBuilder.raw(">")

        generateChildContent(template, tag.children)

        if (tag.children.isNotEmpty()) {
            contentBuilder.raw("</${tag.name}>")
        } else if (!SELF_CLOSING_TAGS.contains(tag.name)) {
            contentBuilder.raw(" />")
        } else {
            contentBuilder.raw(">")
        }

        tag.attrs.filter { it.key in controlAttrs }.forEach { _ ->
            contentBuilder.endControlFlow()
        }
    }

    private fun generateTextContent(text: HtmlElement.Text) {
        logger.debug { "Generating text content: ${text.content}" }
        val content = text.content
        if (expressionParser.hasKotlinExpression(content)) {
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
            _addedImports.add("import ${customTag.packageName}.${customTag.functionName}")
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
            if (param.isContent) {
                val child = tag.children.filterIsInstance<HtmlElement.Tag>().find { it.name == param.name }

                val content = when {
                    child != null -> child.children
                    param.name == "content" -> remainingChildren.toList()
                    else -> null
                }

                if (content.isNullOrEmpty()) {
                    if (param.defaultValue != "null") {
                        error("Missing required content for parameter '${param.name}' for tag '${customTag.name}'")
                    }
                } else if (param == lastParam) {
                    contentBuilder.endTemplateCallWithContent()
                    contentBuilder.startEmbeddedContent()
                    generateChildContent(customTag, content)
                    contentBuilder.endEmbeddedContent()
                } else {
                    contentBuilder.startEmbeddedContent("${param.name} = ")
                    generateChildContent(customTag, content)
                    contentBuilder.endEmbeddedContent(",")
                }
            } else {
                val attr = tag.attrs[param.name] ?: error("Unknown attribute name '${param.name}'")
                when {
                    expressionParser.isKotlinExpression(attr) ->
                        contentBuilder.kotlin("${param.name} = ${expressionParser.extractSingleExpression(attr)},")

                    param.type == "String" -> contentBuilder.kotlin("${param.name} = \"$attr\",")
                    else -> contentBuilder.kotlin("${param.name} = $attr,")
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
    "wbr"
)