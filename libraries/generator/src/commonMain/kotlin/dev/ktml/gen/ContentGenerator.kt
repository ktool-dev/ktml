package dev.ktml.gen

import dev.ktml.TagDefinition
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.parser.removeEmptyText
import dev.ktml.util.isNotVoidTag
import dev.ktml.util.toImport
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.gen.safe
import dev.ktool.gen.types.Block
import dev.ktool.gen.types.Import
import dev.ktool.gen.types.Property
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class TemplateContent(val imports: List<Import>, val body: Block, val templateConstant: Property) {
    val templateConstantIsNotEmpty = templateConstant.initializer?.expression != "$TRIPLE_QUOTE$TRIPLE_QUOTE"
}

/**
 * Generates HtmlWriter method calls from parsed HTML elements
 */
class ContentGenerator(private val templates: Templates) {
    private val contentBuilder = ContentBuilder()
    private val expressionParser = ExpressionParser()
    private val imports = mutableListOf<Import>()
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
            imports = imports.sortedBy { it.packagePath },
            body = Block(contentAndConstants.body.replace("() {", " {")),
            templateConstant = contentAndConstants.templateConstant,
        )
    }

    private fun initializeImports(template: ParsedTemplate) {
        imports.clear()
        imports.addAll(template.imports.map { it.toImport() })

        if (template.parameters.any { it.type == "Content" }) {
            imports.add(Import("dev.ktml.Content"))
        }

        imports.add(Import("dev.ktml.Context"))
    }

    private fun generateContextParams(template: ParsedTemplate) {
        template.parameters.filter { it.isContextParam }.forEach { param ->
            val name = param.name.removePrefix("ctx-")
            logger.debug { "Generating context param: $name" }
            contentBuilder.kotlin(param.contextParameterDefinition())
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

        tag.attrs["if"]?.let {
            contentBuilder.startControlFlow("if", expressionParser.extractSingleExpression(it))
        }

        tag.attrs["each"]?.let {
            contentBuilder.startControlFlow("for", expressionParser.extractSingleExpression(it))
        }

        val customTag = templates.locate(template.subPath, tag.name)

        // This prevents a template from calling itself
        if (customTag != null && !rootTemplate.sameTemplate(customTag)) {
            generateCustomTagCall(template, tag, customTag)
        } else if (tag.isKotlinScript) {
            tag.children.joinToString("") { (it as HtmlElement.Text).content.trim() }
                .split("\n").also { contentBuilder.kotlin(it) }
        } else {
            val currentNoInterpolation = noInterpolation || tag.attrs.containsKey("ignore-kotlin")

            contentBuilder.raw("<${tag.name}")

            tag.attrs.filterNot { it.key in filteredAttrs }.forEach { (name, value) ->
                if (!currentNoInterpolation && expressionParser.hasKotlinExpression(value)) {
                    contentBuilder.raw(" $name=\"")
                    expressionParser.extractMultipleExpressions(value).forEach { part ->
                        if (part.isKotlin) {
                            contentBuilder.write(part.text)
                        } else {
                            contentBuilder.raw(part.text)
                        }
                    }
                    contentBuilder.raw("\"")
                } else {
                    contentBuilder.raw(" $name=\"$value\"")
                }
            }

            contentBuilder.raw(">")

            if (tag.name.isNotVoidTag()) {
                generateChildContent(template, tag.children, currentNoInterpolation)
                contentBuilder.raw("</${tag.name}>")
            }
        }

        tag.attrs.filter { it.key in controlAttrs }.forEach { _ ->
            contentBuilder.endControlFlow()
        }
    }

    private fun generateTextContent(text: HtmlElement.Text, noInterpolation: Boolean = false) {
        logger.debug { "Generating text content: '${text.content}'" }
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

    private fun generateCustomTagCall(template: ParsedTemplate, tag: HtmlElement.Tag, customTag: TagDefinition) {
        logger.debug { "Generating custom tag call: ${customTag.name}" }

        logger.debug { "Custom tag: ${customTag.path}, Template: ${template.path}" }
        if (!template.samePath(customTag)) {
            imports.add(Import("${customTag.packageName}.${customTag.functionName}"))
        }

        contentBuilder.startTemplateCall(customTag.functionName)

        val templateParams = customTag.parameters
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
            val paramName = param.name.safe

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

                if (!content.isNullOrEmpty()) {
                    if (param == lastParam) {
                        contentBuilder.endTemplateCallWithContent()
                        contentBuilder.startEmbeddedContent()
                        generateChildContent(template, content)
                        contentBuilder.endEmbeddedContent()
                    } else {
                        contentBuilder.startEmbeddedContent("$paramName = ")
                        generateChildContent(template, content)
                        contentBuilder.endEmbeddedContent(",")
                    }
                }
            } else if (tag.attrs.containsKey(param.name)) {
                val value = tag.attrs[param.name]
                when {
                    value == null -> contentBuilder.kotlin("$paramName = null,")
                    expressionParser.isKotlinExpression(value) ->
                        contentBuilder.kotlin("$paramName = ${expressionParser.extractSingleExpression(value)},")

                    param.isString -> contentBuilder.kotlin("${param.name} = \"$value\",")
                    else -> contentBuilder.kotlin("$paramName = $value,")
                }
            }
        }

        if (lastParam?.isContent != true) {
            contentBuilder.endTemplateCall()
        }
    }
}
