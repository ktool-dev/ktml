package dev.ktml.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

class HtmlHandler(private val selfClosingTags: Collection<String> = emptyList()) : KsoupHtmlHandler {
    private val elementStack = mutableListOf<HtmlElement.Tag>()
    private var _rootElements = mutableListOf<HtmlElement.Tag>()
    private var currentTextElement: HtmlElement.Text? = null

    val rootElements: List<HtmlElement.Tag>
        get() = _rootElements.toList()

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        currentTextElement = null

        val element = HtmlElement.Tag(name.lowercase(), attributes)

        if (elementStack.isEmpty()) {
            _rootElements.add(element)
        } else {
            elementStack.last().addChild(element)
        }

        if (!selfClosingTags.contains(name)) {
            elementStack.add(element)
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        currentTextElement = null
        if (selfClosingTags.contains(name)) return

        elementStack.removeLastOrNull()
    }

    override fun onText(text: String) {
        // Ignore empty text in root elements
        if (!_rootElements.contains(elementStack.lastOrNull()) || text.isNotBlank()) {
            if (currentTextElement == null) {
                currentTextElement = HtmlElement.Text(text)
                elementStack.lastOrNull()?.addChild(currentTextElement!!)
            } else {
                currentTextElement?.content += text
            }
        }
    }

    override fun onComment(comment: String) {
    }

    override fun onError(error: Exception) {
        throw IllegalArgumentException("HTML parsing error: ${error.message}", error)
    }
}
