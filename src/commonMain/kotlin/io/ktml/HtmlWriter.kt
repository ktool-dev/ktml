package io.ktml

import com.mohamedrejeb.ksoup.entities.KsoupEntities

typealias Content = (HtmlWriter) -> Unit

abstract class HtmlWriter {
    fun text(content: String) = raw(KsoupEntities.encodeHtml(content))
    fun content(content: Content) = also { content(it) }

    abstract fun raw(content: String): HtmlWriter
}

class StringBuilderHtmlWriter(private val out: StringBuilder) : HtmlWriter() {
    override fun raw(content: String) = also { out.append(content) }
}
