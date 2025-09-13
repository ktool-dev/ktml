package dev.ktool.ktml.gen

import dev.ktool.ktml.Templates
import dev.ktool.ktml.parser.HtmlElement
import dev.ktool.ktml.parser.ParsedTemplate

data class TemplateContent(val functionContent: String, val rawConstants: String)

/**
 * Generates HtmlWriter method calls from parsed HTML elements
 */
class ContentGenerator(private val templates: Templates) {
    private val contentBuilder = ContentBuilder()
    private val expressionParser = ExpressionParser()

    fun generateTemplateContent(template: ParsedTemplate): TemplateContent {
        contentBuilder.clear()
        generateChildContent(template, template.root.children)
        return contentBuilder.templateContent
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
        val customTag = templates.locate(tag.name, template)

        if (customTag != null) return generateCustomTagContent(tag, customTag)

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

    private fun generateCustomTagContent(tag: HtmlElement.Tag, template: ParsedTemplate) {
        contentBuilder.startTemplateCall(template.qualifiedFunctionName)
        val params = template.orderedParameters

        if (params.isEmpty()) {
            contentBuilder.endTemplateCall()
            return
        }

        val lastParamIsContent = params.last().type == "Content"

        val passedParams = if (lastParamIsContent) {
            params.dropLast(1)
        } else {
            params
        }

        passedParams.forEach { param ->
            val attr = tag.attrs[param.name] ?: error("Unknown attribute name '${param.name}'")

            when {
                expressionParser.isKotlinExpression(attr) -> {
                    contentBuilder.kotlin("${param.name} = ${expressionParser.extractSingleExpression(attr)},")
                }

                param.type == "String" -> contentBuilder.kotlin("${param.name} = \"$attr\",")
                param.type == "Content" -> {
                    contentBuilder.startEmbeddedContent()
                    generateChildContent(template, tag.children)
                    contentBuilder.endEmbeddedContent(",")
                }

                else -> contentBuilder.kotlin("${param.name} = $attr,")
            }
        }

        if (passedParams.isEmpty()) {
            contentBuilder.deleteLastNewLine()
        }

        if (lastParamIsContent) {
            contentBuilder.endTemplateCallWithContent()
            contentBuilder.startEmbeddedContent()
            generateChildContent(template, tag.children)
            contentBuilder.endEmbeddedContent()
        } else {
            contentBuilder.endTemplateCall()
        }

        return
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