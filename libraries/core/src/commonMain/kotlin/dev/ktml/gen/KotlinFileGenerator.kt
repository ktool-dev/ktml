package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.util.toCamelCase
import dev.ktool.gen.types.*

class KotlinFileGenerator(templates: Templates) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(template: ParsedTemplate) = KotlinFile(template.packageName) {
        val content = contentGenerator.generateTemplateContent(template)

        imports.addAll(content.imports)

        generateFunction(template, content)

        if (template.externalScriptContent.isNotEmpty()) {
            literal(template.externalScriptContent.trim())
        }

        members += content.rawConstants
    }

    private fun KotlinFile.generateFunction(template: ParsedTemplate, content: TemplateContent) =
        function("write${template.name.toCamelCase()}", Type("Context")) {
            modifier(Modifier.Suspend)
            template.nonContextParameters.map { param ->
                param(name = param.name, type = Type(param.type)) {
                    defaultValue =
                        param.defaultValue?.let { ExpressionBody(if (param.type == "String") "\"$it\"" else it) }
                }
            }
            body = FunctionBlock(content.body.statements)
        }
}
