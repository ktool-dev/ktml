package dev.ktml.gen.element

import dev.ktml.TagDefinition
import dev.ktml.parser.HtmlTag
import dev.ktml.parser.appendExpressions
import dev.ktml.parser.removeEmptyText
import dev.ktml.util.replaceTicks
import dev.ktool.gen.safe
import dev.ktool.gen.types.Import
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CustomTagHandler(context: ElementHandlerContext) : BaseTagHandler(context) {
    private var lastFoundTag: TagDefinition? = null

    override fun handles(tag: HtmlTag): Boolean {
        lastFoundTag = templates.locate(template.subPath, tag.name)
        return lastFoundTag != null
    }

    override fun process(tag: HtmlTag) {
        val customTag = lastFoundTag
        require(customTag != null) { "You must call the handles function first." }

        logger.debug { "Generating custom element call: ${customTag.name}" }

        logger.debug { "Custom element: ${customTag.path}, Template: ${template.path}" }
        if (!template.samePath(customTag)) {
            imports.add(Import("${customTag.packageName}.${customTag.functionName}"))
        }

        contentBuilder.startTemplateCall(customTag.functionName)

        val templateParams = customTag.parameters
        val remainingChildren = tag.children.toMutableList()
        val lastParam = templateParams.lastOrNull()
        var templateCallEnded = false

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
                        templateCallEnded = true
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

        if (!templateCallEnded) {
            contentBuilder.endTemplateCall()
        }
    }
}