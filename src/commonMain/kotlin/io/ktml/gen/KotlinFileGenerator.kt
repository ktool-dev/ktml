package io.ktml.gen

import io.ktml.parser.ParsedTemplate

private const val PACKAGE_NAME: String = "io.ktml.templates"

class KotlinFileGenerator(private val contentGenerator: ContentGenerator = ContentGenerator()) {
    fun generateCode(template: ParsedTemplate) = buildString {
        appendLine("package $PACKAGE_NAME")
        appendLine()

        requiredImports(template).forEach { appendLine(it) }
        appendLine()

        appendLine(generateFunction(template))
        appendLine("}")
        appendLine()
    }

    private fun requiredImports(template: ParsedTemplate) = buildList {
        addAll(template.imports)

        if (template.parameters.any { it.type == "Content" }) {
            add("import io.ktml.Content")
        }

        add("import io.ktml.HtmlWriter")
    }.sorted()

    private fun generateFunction(template: ParsedTemplate) = buildString {
        append("fun HtmlWriter.write${template.name.toCamelCase()}(")

        template.parameters.forEach { param ->
            if (!endsWith("(")) {
                append(", ")
            }

            append(param.name).append(": ").append(param.type)

            if (param.defaultValue != null) {
                append(" = ")
                if (param.type == "String") {
                    append('"').append(param.defaultValue).append('"')
                } else {
                    append(param.defaultValue)
                }
            }
        }

        appendLine(") {")

        appendLine(contentGenerator.generateChildrenContent(template.root.children))

        appendLine("}")
    }

    private fun String.toCamelCase(): String {
        return split("-").joinToString("") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}