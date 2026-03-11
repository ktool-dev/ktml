package dev.ktml.gen.element

import dev.ktml.gen.filterAttributesWithHandlers
import dev.ktml.parser.HtmlTag
import dev.ktml.util.isNotVoidTag
import dev.ktml.util.replaceTicks

class DefaultTagHandler(context: ElementHandlerContext) : BaseTagHandler(context) {
    // This handles any tags not handled by other TagHandlers, so should always go last
    override fun handles(tag: HtmlTag) = true

    override fun process(tag: HtmlTag) {
        contentBuilder.raw("<${tag.name}")

        tag.attrs.filterAttributesWithHandlers().forEach { (name, value) ->
            val expression = value.extractExpressions().firstOrNull()?.expression?.kotlinFileContent

            if (expression?.trim()?.startsWith("raw(") == false) {
                contentBuilder.startEmbeddedContent("(${expression.replaceTicks()})?.let ")
                contentBuilder.raw(" $name=\"")
                contentBuilder.write("it")
                contentBuilder.raw("\"")
                contentBuilder.endEmbeddedContent()
            } else {
                contentBuilder.raw(" $name=\"")
                value.writeExpressions(true)
                contentBuilder.raw("\"")
            }
        }

        contentBuilder.raw(">")

        if (tag.name.isNotVoidTag()) {
            generateChildContent(tag.children)
            contentBuilder.raw("</${tag.name}>")
        }
    }
}
