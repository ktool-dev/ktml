package dev.ktml.gen.element

import dev.ktml.gen.ContentBuilder
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.util.replaceTicks
import dev.ktool.gen.types.Import

private val HANDLER_CREATORS = listOf<(ElementHandlerContext) -> HtmlElementHandler>(
    ::ContextTagHandler,
    ::ScriptTagHandler,
    ::TextElementHandler,
    ::CustomTagHandler,
    ::DefaultTagHandler,
)

fun buildHtmlElementHandlers(
    template: ParsedTemplate,
    templates: Templates,
    contentBuilder: ContentBuilder,
    imports: MutableList<Import>,
): List<HtmlElementHandler> {
    val handlers = mutableListOf<HtmlElementHandler>()
    val childHandler = { elements: List<HtmlElement> -> handlers.process(elements) }

    val context = ElementHandlerContext(template, templates, contentBuilder, imports, childHandler)

    HANDLER_CREATORS.forEach { handlers.add(it.invoke(context)) }
    
    return handlers
}

typealias ChildHandler = (List<HtmlElement>) -> Unit

fun List<HtmlElementHandler>.process(elements: List<HtmlElement>) = elements.forEach { element ->
    find { it.handles(element) }?.process(element) ?: error("No handler found for $element")
}

data class ElementHandlerContext(
    val template: ParsedTemplate,
    val templates: Templates,
    val contentBuilder: ContentBuilder,
    val imports: MutableList<Import>,
    val childHandler: ChildHandler,
)

interface HtmlElementHandler {
    fun handles(element: HtmlElement): Boolean

    fun process(element: HtmlElement)
}

abstract class BaseHtmlElementHandler(context: ElementHandlerContext) : HtmlElementHandler {
    val contentBuilder = context.contentBuilder
    val template = context.template

    fun String.writeExpressions(replaceTicks: Boolean = false) {
        extractExpressions().forEach {
            if (it.text != null) {
                contentBuilder.raw(it.text)
            } else if (it.expression != null) {
                val value = it.expression.kotlinFileContent
                contentBuilder.write(if (replaceTicks) value.replaceTicks() else value)
            }
        }
    }

    fun String.extractExpressions() = template.extractExpressions(this)
}
