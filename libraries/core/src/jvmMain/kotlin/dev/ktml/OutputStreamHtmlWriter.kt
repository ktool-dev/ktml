package dev.ktml

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

class OutputStreamHtmlWriter(out: OutputStream) : ContentWriter {
    private val out: Writer = OutputStreamWriter(out)

    override fun write(content: String) {
        out.write(content)
    }
}