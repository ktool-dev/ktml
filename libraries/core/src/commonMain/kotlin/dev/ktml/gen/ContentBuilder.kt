package dev.ktml.gen

import dev.ktml.INDENTATION
import dev.ktml.TRIPLE_QUOTE

internal const val RAW_PREFIX = "RAW_CONTENT_"

data class ContentAndRawConstants(val content: String, val rawConstants: List<RawConstant>)

data class RawConstant(val index: Int, val content: String) {
    override fun toString() = "private const val $RAW_PREFIX$index = ${TRIPLE_QUOTE}$content${TRIPLE_QUOTE}"
}

fun List<RawConstant>.toContentString() = joinToString("\n") { it.toString() }.trim()

/**
 * Builds the content of a template function. This helps with indentation and grouping raw content together.
 */
class ContentBuilder {
    private val rawContentItems = mutableListOf<String>()
    private val currentRawContent = StringBuilder()
    private val builder = StringBuilder(INDENTATION)

    private var writingRaw = false
    private var indent = INDENTATION

    fun clear() {
        currentRawContent.clear()
        rawContentItems.clear()
        builder.clear().append(INDENTATION)
        writingRaw = false
        indent = INDENTATION
    }

    fun raw(content: String) {
        if (!writingRaw) {
            writingRaw = true
            builder.append("raw($RAW_PREFIX${rawContentItems.size})").newline()
        }
        currentRawContent.append(content)
    }

    fun doctype(content: String) {
        currentRawContent.clear()
        raw(content)
        currentRawContent.appendLine()
    }

    private fun endRaw() {
        if (writingRaw) {
            rawContentItems.add(currentRawContent.toString())
            currentRawContent.clear()
            writingRaw = false
        }
    }

    val templateContent: ContentAndRawConstants
        get() {
            endRaw()

            val constants = rawContentItems.mapIndexed { index, it ->
                RawConstant(index, it)
            }

            return ContentAndRawConstants(builder.toString().trimMargin(), constants)
        }

    private fun StringBuilder.newline(): StringBuilder {
        appendLine()
        return append(indent)
    }

    fun write(kotlin: String) {
        endRaw()
        builder.append("write(").append(kotlin).append(")").newline()
    }

    fun startControlFlow(type: String, condition: String) {
        endRaw()
        builder.append("$type ($condition) {")
        increaseIndentation()
        builder.newline()
    }

    fun endControlFlow() {
        endRaw()
        decreaseIndentation()
        removeOneIndention()
        builder.append("}").newline()
    }

    fun startTemplateCall(functionName: String) {
        endRaw()
        increaseIndentation()
        builder.append("$functionName(").newline()
    }

    fun deleteLastNewLine() {
        builder.setLength(builder.length - (builder.length - builder.lastIndexOf('\n')))
    }

    fun endTemplateCall() {
        decreaseIndentation()
        if (builder.endsWith(INDENTATION)) {
            builder.setLength(builder.length - INDENTATION.length)
        }
        builder.append(")").newline()
    }

    fun endTemplateCallWithContent() {
        decreaseIndentation()
        removeOneIndention()
        builder.append(") ")
    }

    fun startEmbeddedContent(prefix: String = "") {
        endRaw()
        increaseIndentation()
        builder.append(prefix).append("{").newline()
    }

    fun endEmbeddedContent(suffix: String = "") {
        endRaw()
        decreaseIndentation()
        removeOneIndention()
        builder.append("}").append(suffix).newline()
    }

    private fun removeOneIndention() {
        if (builder.endsWith(INDENTATION)) {
            builder.setLength(builder.length - INDENTATION.length)
        }
    }

    fun kotlin(kotlin: String) {
        endRaw()
        builder.append(kotlin).newline()
    }

    fun kotlin(kotlin: List<String>) {
        endRaw()
        kotlin.forEach { builder.append(it.trim()).newline() }
    }

    private fun increaseIndentation() {
        indent += INDENTATION
    }

    private fun decreaseIndentation() {
        indent = indent.substring(0, indent.length - INDENTATION.length)
    }
}
