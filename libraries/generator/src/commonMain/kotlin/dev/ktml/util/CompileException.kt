package dev.ktml.util


data class CompilerError(val message: String, val filePath: String, val line: Int, val column: Int) {
    fun toTemplateMessage() = "Error in File $filePath: $message"
}

class CompileException(val errors: List<CompilerError>) :
    Exception("Compilation failed:\n${errors.joinToString("\n") { it.toTemplateMessage() }}")
