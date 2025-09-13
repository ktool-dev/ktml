package dev.ktool.ktml

import okio.Buffer

class BufferHtmlHandler(private val buffer: Buffer) {
    fun write(content: String) {
        buffer.writeUtf8(content)
    }
}