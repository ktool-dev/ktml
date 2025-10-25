package dev.ktml.gen

import dev.ktml.TagDefinition
import dev.ktml.parser.*
import dev.ktml.util.isNotVoidTag
import dev.ktml.util.replaceTicks
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
 * Generates HtmlWriter method calls from parsed HTML elements. This class is not thread save.
 */
class ContentGenerator(private val templates: Templates, private val template: ParsedTemplate) {
    private val contentBuilder = ContentBuilder()
    private val imports = mutableListOf<Import>()

    fun generate(): TemplateContent {
        logger.debug { "Generating content for template: ${template.name}" }
        reset()
        initializeImports()
        generateContextParams()

        if (template.dockTypeDeclaration.isNotBlank()) {
            contentBuilder.doctype(template.dockTypeDeclaration)
        }

        generateChildContent(template.root.children)

        val contentAndConstants = contentBuilder.templateContent
        logger.debug { "Finished generating content for template: ${template.name}" }

        return TemplateContent(
            imports = imports.sortedBy { it.packagePath },
            body = Block(contentAndConstants.body.replace("() {", " {")),
            templateConstant = contentAndConstants.templateConstant,
        )
    }

    private fun reset() {
        contentBuilder.clear()
        imports.clear()
    }

    private fun initializeImports() {
        imports.addAll(template.imports.map { it.toImport() })

        if (template.parameters.any { it.isContent }) {
            imports.add(Import("dev.ktml.Content"))
        }

        imports.add(Import("dev.ktml.Context"))
    }

    private fun generateContextParams() {
        template.parameters.filter { it.isContextParam }.forEach { param ->
            logger.debug { "Generating context param: ${param.name}" }
            contentBuilder.kotlin(param.contextParameterDefinition().replaceTicks())
        }
    }

    private fun generateChildContent(children: List<HtmlElement>) {
        children.forEach {
            when (it) {
                is HtmlTag -> generateTagContent(it)
                is HtmlText -> generateTextContent(it)
            }
        }
    }

    private fun generateTagContent(tag: HtmlTag) {
        logger.debug { "Generating tag content: ${tag.name}" }

        findTagHandler(tag)?.let {
            it.process(template, tag, contentBuilder, ::generateChildContent)
            return@generateTagContent
        }

        val specialAttributes = matchingSpecialAttribute(tag.attrs).also {
            it.forEach { (attribute, value) -> attribute.process(template, contentBuilder, value) }
        }.map { it.first }

        val customTag = templates.locate(template.subPath, tag.name)

        if (customTag != null) {
            generateCustomTagCall(tag, customTag)
        } else {
            generateBasicTagContent(tag)
        }

        specialAttributes.forEach { if (it.isBlock) contentBuilder.endBlock() }
    }

    private fun generateBasicTagContent(tag: HtmlTag) {
        contentBuilder.raw("<${tag.name}")

        tag.attrs.filterAttributesWithHandlers().forEach { (name, value) ->
            contentBuilder.raw(" $name=\"")
            value.writeExpressions(true)
            contentBuilder.raw("\"")
        }

        contentBuilder.raw(">")

        if (tag.name.isNotVoidTag()) {
            generateChildContent(tag.children)
            contentBuilder.raw("</${tag.name}>")
        }
    }

    private fun String.writeExpressions(replaceTicks: Boolean = false) {
        extractExpressions().forEach {
            if (it.text != null) {
                contentBuilder.raw(it.text)
            } else if (it.expression != null) {
                val value = it.expression.kotlinFileContent
                contentBuilder.write(if (replaceTicks) value.replaceTicks() else value)
            }
        }
    }

    private fun String.extractExpressions() = template.extractExpressions(this)

    private fun generateTextContent(text: HtmlText) {
        logger.debug { "Generating text content: '${text.content}'" }
        text.content.writeExpressions()
    }

    private fun generateCustomTagCall(tag: HtmlTag, customTag: TagDefinition) {
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
                val child = tag.children.filterIsInstance<HtmlTag>().find { it.name == param.name }

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
                        generateChildContent(content)
                        contentBuilder.endEmbeddedContent()
                    } else {
                        contentBuilder.startEmbeddedContent("$paramName = ")
                        generateChildContent(content)
                        contentBuilder.endEmbeddedContent(",")
                    }
                }
            } else if (tag.attrs.containsKey(param.name)) {
                val expressions = tag.attrs[param.name]?.trim()?.extractExpressions()
                when {
                    expressions == null || expressions.isEmpty() -> contentBuilder.kotlin("$paramName = null,")

                    expressions.size == 1 && expressions[0].expression != null -> contentBuilder.kotlin(
                        "$paramName = ${expressions[0].expression?.kotlinFileContent?.replaceTicks()},"
                    )

                    expressions.size > 1 -> contentBuilder.kotlin(buildString {
                        append("$paramName = ")
                        appendExpressions(expressions)
                    })

                    param.isString -> contentBuilder.kotlin("$paramName = \"${expressions[0].text}\",")
                    else -> contentBuilder.kotlin("$paramName = ${expressions[0].text},")
                }
            }
        }

        if (lastParam?.isContent != true) {
            contentBuilder.endTemplateCall()
        }
    }
}
