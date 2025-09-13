package dev.ktool.ktml.gen

import dev.ktool.ktml.INDENTATION
import dev.ktool.ktml.Templates
import dev.ktool.ktml.parser.ParsedTemplate
import dev.ktool.ktml.toCamelCase

class KotlinFileGenerator(templates: Templates) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(template: ParsedTemplate) = buildString {
        appendLine("package ${template.packageName}")
        appendLine()

        requiredImports(template).forEach { appendLine(it) }
        appendLine()

        if (template.topExternalScriptContent.isNotEmpty()) {
            appendLine(template.topExternalScriptContent)
            appendLine()
        }

        val (functionContent, rawConstants) = generateFunction(template)
        appendLine(functionContent)
        appendLine()

        if (template.bottomExternalScriptContent.isNotEmpty()) {
            appendLine(template.bottomExternalScriptContent)
            appendLine()
        }

        appendLine(rawConstants)
    }

    private fun requiredImports(template: ParsedTemplate) = buildSet {
        addAll(template.imports)

        if (template.parameters.any { it.type == "Content" }) {
            add("import dev.ktool.ktml.Content")
        }

        add("import dev.ktool.ktml.Context")
    }.sorted()

    private fun generateFunction(template: ParsedTemplate): Pair<String, String> {
        val content = contentGenerator.generateTemplateContent(template)

        val functionContent = buildString {
            append("fun Context.write${template.name.toCamelCase()}(")

            template.parameters.forEach { param ->
                appendLine().append(INDENTATION).append(param.name).append(": ").append(param.type)

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

            if (template.parameters.isNotEmpty()) {
                appendLine()
            }

            appendLine(") {")
            appendLine(content.functionContent)
            append("}")
        }

        return functionContent to content.rawConstants
    }
}
