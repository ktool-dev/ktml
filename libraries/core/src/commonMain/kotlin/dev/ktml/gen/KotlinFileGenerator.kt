package dev.ktml.gen

import dev.ktml.INDENTATION
import dev.ktml.Templates
import dev.ktml.parser.ParsedTemplate
import dev.ktml.toCamelCase

class KotlinFileGenerator(templates: Templates) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(template: ParsedTemplate) = buildString {
        val content = contentGenerator.generateTemplateContent(template)

        appendLine("package ${template.packageName}")
        appendLine()

        requiredImports(template).plus(contentGenerator.addedImports).sorted().forEach { appendLine(it) }
        appendLine()

        if (template.topExternalScriptContent.isNotEmpty()) {
            appendLine(template.topExternalScriptContent)
            appendLine()
        }

        val (functionContent, rawConstants) = generateFunction(template, content)
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
            add("import dev.ktml.Content")
        }

        add("import dev.ktml.Context")
    }.sorted()

    private fun generateFunction(template: ParsedTemplate, content: TemplateContent): Pair<String, String> {
        val functionContent = buildString {
            append("fun Context.write${template.name.toCamelCase()}(")

            val functionParams = template.parameters.filterNot { it.isContextParam }

            functionParams.forEach { param ->
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

            if (functionParams.isNotEmpty()) {
                appendLine()
            }

            appendLine(") {")
            appendLine(content.functionContent)
            append("}")
        }

        return functionContent to content.rawConstants
    }
}
