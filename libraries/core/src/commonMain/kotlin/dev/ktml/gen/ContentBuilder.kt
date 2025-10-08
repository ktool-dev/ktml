package dev.ktml.gen

import dev.ktool.gen.CodeWriter
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.gen.types.ExpressionBody
import dev.ktool.gen.types.Modifier
import dev.ktool.gen.types.Property
import dev.ktool.gen.types.StringType

internal const val RAW_PREFIX = "RAW_CONTENT_"

data class ContentAndRawConstants(val body: String, val rawConstants: List<Property>)

data class RawConstant(val index: Int, val content: String) {
    fun toProperty() = Property(
        name = "$RAW_PREFIX$index",
        type = StringType,
        initializer = ExpressionBody("$TRIPLE_QUOTE$content$TRIPLE_QUOTE"),
        modifiers = listOf(Modifier.Private, Modifier.Const)
    )
}

/**
 * Builds the content of a template function. This helps with indentation and grouping raw content together.
 */
class ContentBuilder {
    private val rawContentItems = mutableListOf<String>()
    private val currentRawContent = StringBuilder()
    private var writer = CodeWriter()

    private var writingRaw = false

    fun clear() {
        currentRawContent.clear()
        rawContentItems.clear()
        writingRaw = false
        writer = CodeWriter()
    }

    fun raw(content: String) {
        if (!writingRaw) {
            writingRaw = true
            writer.write("raw($RAW_PREFIX${rawContentItems.size})").newLine()
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

            return ContentAndRawConstants(writer.toString(), constants.map { it.toProperty() })
        }

    fun write(kotlin: String) {
        endRaw()
        if (kotlin.trim().startsWith("raw(")) {
            writer.write(kotlin).newLine()
        } else {
            writer.write("write(").write(kotlin).write(")").newLine()
        }
    }

    fun startControlFlow(type: String, condition: String) {
        endRaw()
        writer.write("$type ($condition) {")
        writer.indent()
        writer.newLine()
    }

    fun endControlFlow() {
        endRaw()
        writer.unindent()
        writer.removeLastIndentation()
        writer.write("}").newLine()
    }

    fun startTemplateCall(functionName: String) {
        endRaw()
        writer.indent()
        writer.write("$functionName(").newLine()
    }

    fun deleteLastNewLine() {
        writer.trimEnd()
    }

    fun endTemplateCall() {
        writer.unindent()
        writer.removeLastIndentation()
        writer.write(")").newLine()
    }

    fun endTemplateCallWithContent() {
        writer.unindent()
        writer.removeLastIndentation()
        writer.write(") ")
    }

    fun startEmbeddedContent(prefix: String = "") {
        endRaw()
        writer.indent()
        writer.write(prefix).write("{").newLine()
    }

    fun endEmbeddedContent(suffix: String = "") {
        endRaw()
        writer.unindent()
        writer.removeLastIndentation()
        writer.write("}").write(suffix).newLine()
    }

    fun kotlin(kotlin: String) {
        endRaw()
        writer.write(kotlin).newLine()
    }

    fun kotlin(kotlin: List<String>) {
        endRaw()
        kotlin.forEach { writer.write(it.trim()).newLine() }
    }
}
