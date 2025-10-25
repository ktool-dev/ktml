package dev.ktml.gen.element

import dev.ktml.parser.HtmlTag
import dev.ktml.parser.buildString
import dev.ktml.util.replaceTicks

private const val NAME = "context"

class ContextTagHandler(context: ElementHandlerContext) : BaseTagHandler(context) {
    override fun handles(tag: HtmlTag) = tag.name == NAME

    override fun process(tag: HtmlTag) {
        val clear = tag.attrs.any { (key, value) -> key == "clear" && value != "false" }

        require(!clear || tag.children.isNotEmpty()) { "You cannot set clear on a '$NAME' element unless it has children" }

        fun convertValue(value: String): Any? {
            if (value == "null") return null

            val expressions = template.extractExpressions(value)
            return when {
                expressions.size == 1 && expressions[0].expression != null -> expressions[0].expression?.kotlinFileContent?.replaceTicks()
                expressions.size > 1 -> expressions.buildString()
                else -> "\"$value\""
            }
        }

        if (tag.children.isEmpty()) {
            tag.attrs.forEach {
                contentBuilder.kotlin("set(\"${it.key}\", ${convertValue(it.value)})")
            }
        } else {
            contentBuilder.kotlin("copy(clear = $clear, params = mapOf(")
            tag.attrs.filterNot { it.key == "clear" }.forEach {
                contentBuilder.kotlin("""    "${it.key}" to ${convertValue(it.value)}, """)
            }
            contentBuilder.startEmbeddedContent(")).write ")
            generateChildContent(tag.children)
            contentBuilder.endEmbeddedContent()
        }
    }
}