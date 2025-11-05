package dev.ktml.util

import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.kotlin.ID_COMMENT_REGEX
import dev.ktml.parser.kotlin.KotlinExpression
import dev.ktool.gen.LINE_SEPARATOR

class CompilerErrorResolver(
    private val parsedTemplates: List<ParsedTemplate>,
    templatePackage: String,
    generatedDir: String,
    templateDir: String
) {
    private val generatedDir = generatedDir.toPath()
    private val templateDir = templateDir.toPath()
    private val templatePackagePath = templatePackage.replace(".", "/")

    fun resolve(errors: List<CompilerError>): List<CompilerError> {
        val errorsAndExpressions = errors.map { error ->
            val codeFile = error.filePath.substringAfter(templatePackagePath).removePrefix("/")
            val template = findTemplateByCodeFile(codeFile) ?: error("Could not find template for $codeFile")
            Triple(resolveExpression(template, error), template, error)
        }.groupBy { it.first?.uuid }.values.map { list ->
            Triple(list.first().first, list.first().second, list.map { it.third })
        }

        return errorsAndExpressions.map { (expression, template, errors) ->
            if (expression == null) {
                val message = buildString {
                    appendLine()
                    appendLine("  Code above the template tags had these errors:")
                    errors.forEach { appendLine("  • ${it.message}") }
                    appendLine("Here's the content:")
                    appendLine(template.externalScriptContent)
                }

                CompilerError(filePath = template.templateFile, message = message)
            } else {
                val templateContent = findTemplateContent(template, expression)

                val message = buildString {
                    appendLine()
                    append("The expression ")
                    if (expression.isMultiLine) append("starting at") else append("on")
                    append(" line ")
                    append(expression.start.line)
                    if (expression.isMultiLine) append(" column ") else append(" starting at column ")
                    append(expression.start.column)
                    appendLine(" of the template had these errors:")
                    errors.forEach { appendLine(" • ${it.message}") }
                    appendLine()
                    appendLine("Here's the content:")
                    appendLine(templateContent)
                }

                CompilerError(
                    filePath = template.templateFile,
                    message = message,
                    line = expression.start.line,
                    column = expression.start.column
                )
            }
        }
    }

    private fun findTemplateByCodeFile(codeFile: String) = parsedTemplates.find { it.codeFile == codeFile }

    private fun resolveExpression(template: ParsedTemplate, error: CompilerError): KotlinExpression? {
        return findExpressionId(error)?.let { template.findExpression(it) }
    }

    private fun findExpressionId(error: CompilerError): String? {
        val lines = generatedDir.resolve(error.filePath).readLines()
        var currentLine = error.line - 1
        var line = lines[currentLine].substring(error.column - 1)
        while (!ID_COMMENT_REGEX.containsMatchIn(line) && currentLine + 1 < lines.size) {
            line = lines[++currentLine]
        }

        return ID_COMMENT_REGEX.find(line)?.groups?.get(1)?.value
    }

    private fun findTemplateContent(template: ParsedTemplate, expression: KotlinExpression): String {
        val lines = templateDir.resolve(template.templateFile).readLines()
        return (expression.start.line..expression.end.line).joinToString(LINE_SEPARATOR) { lines[it] }
    }
}