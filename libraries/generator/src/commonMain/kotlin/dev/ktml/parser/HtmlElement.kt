package dev.ktml.parser

/**
 * Represents an HTML element in the parsed template
 */
sealed class HtmlElement

data class HtmlTag(
    val name: String,
    val attrs: Map<String, String>,
    private val _children: MutableList<HtmlElement> = mutableListOf()
) : HtmlElement() {
    val children: List<HtmlElement> get() = _children.toList()

    fun addChild(element: HtmlElement) = _children.add(element)
}

data class HtmlText(var content: String) : HtmlElement()

fun List<HtmlElement>.removeEmptyText() = filterNot { it is HtmlText && it.content.isBlank() }
