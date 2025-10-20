package dev.ktml.gen

import dev.ktool.gen.CodeWriter
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.gen.types.ExpressionBody
import dev.ktool.gen.types.Modifier
import dev.ktool.gen.types.Property
import dev.ktool.gen.types.StringType

internal const val TEMPLATE_CONSTANT = "TEMPLATE_HTML"

data class ContentAndRawConstants(val body: String, val templateConstant: Property)

/**
 * Builds the content of a template function. This helps with indentation and grouping raw content together.
 */
class ContentBuilder {
    private val allRawContent = StringBuilder()
    private val currentRawContent = StringBuilder()
    private var writer = CodeWriter()

    private var writingRaw = false

    fun clear() {
        currentRawContent.clear()
        allRawContent.clear()
        writingRaw = false
        writer = CodeWriter()
    }

    fun raw(content: String) {
        if (!writingRaw) {
            writingRaw = true
            writer.write("raw($TEMPLATE_CONSTANT, ${allRawContent.length}, ")
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
            allRawContent.append(currentRawContent.toString())
            writer.write("${currentRawContent.length})").newLine()
            currentRawContent.clear()
            writingRaw = false
        }
    }

    val templateContent: ContentAndRawConstants
        get() {
            endRaw()

            return ContentAndRawConstants(
                writer.toString(), Property(
                    name = TEMPLATE_CONSTANT,
                    type = StringType,
                    initializer = ExpressionBody("$TRIPLE_QUOTE$allRawContent$TRIPLE_QUOTE"),
                    modifiers = listOf(Modifier.Private, Modifier.Const)
                )
            )
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
