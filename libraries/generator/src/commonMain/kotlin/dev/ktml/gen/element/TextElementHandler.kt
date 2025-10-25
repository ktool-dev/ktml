package dev.ktml.gen.element

import dev.ktml.parser.HtmlElement
import dev.ktml.parser.HtmlText
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class TextElementHandler(context: ElementHandlerContext) : BaseHtmlElementHandler(context) {
    override fun handles(element: HtmlElement) = element is HtmlText

    override fun process(element: HtmlElement) {
        val text = element as? HtmlText ?: error("Unexpected element type: ${element::class}")
        logger.debug { "Generating text content: '${text.content}'" }
        text.content.writeExpressions()
    }
}