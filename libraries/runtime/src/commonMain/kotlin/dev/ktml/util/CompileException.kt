package dev.ktml.util

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktml.templates.writeCompileException


data class CompilerError(val message: String, val filePath: String, val line: Int = 0, val column: Int = 0) {
    fun toTemplateMessage() = "[$filePath] $message"
}

class CompileException(val errors: List<CompilerError>) :
    Exception("Compilation failed:\n${errors.joinToString("\n") { it.toTemplateMessage() }}")

class CompileExceptionRegistry(val exception: CompileException) : KtmlRegistry {
    override fun get(path: String): Content = {
        this["exception"] = exception
        writeCompileException()
    }

    override val tags: List<TagDefinition> = listOf()
}
