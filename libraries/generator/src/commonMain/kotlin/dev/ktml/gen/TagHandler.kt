package dev.ktml.gen

import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.buildString
import dev.ktml.util.replaceTicks

private val HANDLERS = listOf(ContextTagHandler, ScriptTagHandler)

fun findTagHandler(tag: HtmlElement.Tag) = HANDLERS.find { it.handles(tag) }

interface TagHandler {
    fun handles(tag: HtmlElement.Tag): Boolean

    fun process(
        template: ParsedTemplate,
        tag: HtmlElement.Tag,
        content: ContentBuilder,
        childrenHandler: (List<HtmlElement>) -> Unit,
    )
}

object ContextTagHandler : TagHandler {
    private const val NAME = "context"
    override fun handles(tag: HtmlElement.Tag) = tag.name == NAME

    override fun process(
        template: ParsedTemplate,
        tag: HtmlElement.Tag,
        content: ContentBuilder,
        childrenHandler: (List<HtmlElement>) -> Unit,
    ) {
        val clear = tag.attrs.any { (key, value) -> key == "clear" && value != "false" }

        require(!clear || tag.children.isNotEmpty()) { "You cannot set clear on a '$NAME' tag unless it has children" }

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
                content.kotlin("set(\"${it.key}\", ${convertValue(it.value)})")
            }
        } else {
            content.kotlin("copy(clear = $clear, params = mapOf(")
            tag.attrs.filterNot { it.key == "clear" }.forEach {
                content.kotlin("""    "${it.key}" to ${convertValue(it.value)}, """)
            }
            content.startEmbeddedContent(")).write ")
            childrenHandler(tag.children)
            content.endEmbeddedContent()
        }
    }
}

object ScriptTagHandler : TagHandler {
    override fun handles(tag: HtmlElement.Tag) = tag.name == "script" && tag.attrs["type"] == "text/kotlin"

    override fun process(
        template: ParsedTemplate,
        tag: HtmlElement.Tag,
        content: ContentBuilder,
        childrenHandler: (List<HtmlElement>) -> Unit
    ) {
        val textNode = tag.children[0] as HtmlElement.Text
        content.kotlin(textNode.content.trim())
    }
}
