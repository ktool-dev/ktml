package dev.ktml.gen.element

import dev.ktml.gen.matchingSpecialAttribute
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.HtmlTag
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

abstract class BaseTagHandler(context: ElementHandlerContext) : BaseHtmlElementHandler(context) {
    val templates = context.templates
    val imports = context.imports
    private val childHandler = context.childHandler

    fun generateChildContent(children: List<HtmlElement>) = childHandler(children)

    override fun handles(element: HtmlElement) = element is HtmlTag && handles(element)

    abstract fun handles(tag: HtmlTag): Boolean
    abstract fun process(tag: HtmlTag)

    override fun process(element: HtmlElement) {
        require(element is HtmlTag) { "$this is not a tag" }
        
        logger.debug { "Generating element content: ${element.name}" }

        val specialAttributes = matchingSpecialAttribute(element.attrs).also {
            it.forEach { (attribute, value) -> attribute.process(template, contentBuilder, value) }
        }.map { it.first }

        process(element)

        specialAttributes.forEach { if (it.isBlock) contentBuilder.endBlock() }
    }
}
