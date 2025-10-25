package dev.ktml.util


data class CompilerError(val message: String, val filePath: String, val line: Int = 0, val column: Int = 0) {
    fun toTemplateMessage() = "[$filePath] $message"
}

class CompileException(val errors: List<CompilerError>) :
    Exception("Compilation failed:\n${errors.joinToString("\n") { it.toTemplateMessage() }}")
