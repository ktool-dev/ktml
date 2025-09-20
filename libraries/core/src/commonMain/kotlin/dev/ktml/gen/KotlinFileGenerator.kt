package dev.ktml.gen

import dev.ktml.INDENTATION
import dev.ktml.Templates
import dev.ktml.escapeIfKeyword
import dev.ktml.parser.ParsedTemplate
import dev.ktml.toCamelCase

class KotlinFileGenerator(templates: Templates) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(template: ParsedTemplate) = buildString {
        val content = contentGenerator.generateTemplateContent(template)

        appendLine("package ${template.packageName}")
        appendLine()

        content.imports.forEach { appendLine(it) }
        appendLine()

        if (template.topExternalScriptContent.isNotEmpty()) {
            appendLine(template.topExternalScriptContent)
            appendLine()
        }

        val (functionContent, rawConstants) = generateFunction(template, content)
        appendLine(functionContent)

        if (template.bottomExternalScriptContent.isNotEmpty()) {
            appendLine()
            appendLine(template.bottomExternalScriptContent)
        }

        if (rawConstants.isNotEmpty()) {
            appendLine()
            appendLine(rawConstants)
        }
    }

    private fun generateFunction(template: ParsedTemplate, content: TemplateContent): Pair<String, String> {
        val functionContent = buildString {
            append("fun Context.write${template.name.toCamelCase()}(")

            val functionParams = template.orderedParameters.filterNot { it.isContextParam }

            functionParams.forEach { param ->
                val paramName = escapeIfKeyword(param.name)
                appendLine().append(INDENTATION).append(paramName).append(": ").append(param.type)

                if (param.defaultValue != null) {
                    append(" = ")
                    if (param.type == "String") {
                        append('"').append(param.defaultValue).append('"')
                    } else {
                        append(param.defaultValue)
                    }
                }
                append(",")
            }

            if (functionParams.isNotEmpty()) {
                appendLine()
            }

            appendLine(") {")
            appendLine(content.functionContent)
            append("}")
        }

        return functionContent to content.rawConstants.toContentString()
    }
}
