package io.ktml

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

class OutputStreamHtmlWriter(out: OutputStream) : HtmlWriter() {
    private val out: Writer = OutputStreamWriter(out)

    override fun raw(content: String) = also { out.write(content) }
}