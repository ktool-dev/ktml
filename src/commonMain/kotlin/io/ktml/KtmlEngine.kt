package io.ktml

import io.ktml.parser.ParsedTemplate
import io.ktml.parser.TemplateParser

class KtmlEngine() {
    fun processTemplate(content: String, rootName: String = ""): List<ParsedTemplate> {
        val parser = TemplateParser()
        return parser.parseContent(content, rootName)
    }
}

/**
 * Result of template processing
 */
data class TemplateProcessingResult(
    val success: Boolean,
    val generatedFiles: Map<String, String>,
    val errors: List<String>
)

/**
 * Result of template validation
 */
data class TemplateValidationResult(
    val valid: Boolean,
    val errors: List<String>
)

/**
 * Result of code generation
 */
data class CodeGenerationResult(
    val success: Boolean,
    val generatedFiles: Map<String, String>,
    val errors: List<String>
)
