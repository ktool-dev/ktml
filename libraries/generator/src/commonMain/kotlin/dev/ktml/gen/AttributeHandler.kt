package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktml.util.replaceTicks

val ATTRIBUTE_HANDLERS: List<AttributeHandler> = listOf(IfAttributeHandler, EachAttributeHandler)

fun matchingSpecialAttribute(attrs: Map<String, String>) = buildList {
    ATTRIBUTE_HANDLERS.forEach { attribute ->
        attrs.forEach { (name, value) ->
            if (name.equals(attribute.name, ignoreCase = true)) {
                add(Pair(attribute, value))
            }
        }
    }
}

fun Map<String, String>.filterAttributesWithHandlers() =
    filter { (key, _) -> ATTRIBUTE_HANDLERS.none { it.name.equals(key, ignoreCase = true) } }

interface AttributeHandler {
    val name: String
    val isBlock: Boolean
    fun process(template: ParsedTemplate, content: ContentBuilder, value: String)
}

abstract class ControlAttributeHandler(override val name: String, val control: String = name) : AttributeHandler {
    override val isBlock = true

    override fun process(template: ParsedTemplate, content: ContentBuilder, value: String) {
        val expression = template.findExpression(value)
            ?: error($$"'$$name' attributes must have a kotlin expression, so it must start with a $")

        content.startControlFlow(control, expression.kotlinFileContent.replaceTicks())
    }
}

object IfAttributeHandler : ControlAttributeHandler("if")

object EachAttributeHandler : ControlAttributeHandler("each", "for")
