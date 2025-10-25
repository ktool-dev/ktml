package dev.ktml.gen.element

import dev.ktml.parser.HtmlTag
import dev.ktml.parser.HtmlText

class ScriptTagHandler(context: ElementHandlerContext) : BaseTagHandler(context) {
    override fun handles(tag: HtmlTag) = tag.name == "script" && tag.attrs["type"] == "text/kotlin"

    override fun process(tag: HtmlTag) {
        val textNode = tag.children[0] as HtmlText
        contentBuilder.kotlin(textNode.content.trim())
    }
}