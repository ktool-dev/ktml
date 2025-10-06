package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateDefinitions
import dev.ktml.util.toCamelCase
import dev.ktool.gen.types.*

class KotlinFileGenerator(templates: TemplateDefinitions) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(packageName: String, template: ParsedTemplate) = KotlinFile(packageName) {
        val content = contentGenerator.generateTemplateContent(template)

        imports.addAll(content.imports)

        if (template.nonContextParameters.isNotEmpty()) {
            generateNoParametersFunction(template)
        }

        generateFunction(template, content)

        if (template.externalScriptContent.isNotEmpty()) {
            literal(template.externalScriptContent.trim())
        }

        members += content.rawConstants
    }

    private fun KotlinFile.generateFunction(template: ParsedTemplate, content: TemplateContent) =
        function("write${template.name.toCamelCase()}", Type("Context")) {
            template.nonContextParameters.map { param ->
                param(name = param.name, type = Type(param.type)) {
                    defaultValue =
                        param.defaultValue?.let { ExpressionBody(if (param.type == "String") "\"$it\"" else it) }
                }
            }
            body = FunctionBlock(content.body.statements)
        }

    private fun KotlinFile.generateNoParametersFunction(template: ParsedTemplate) =
        function("write${template.name.toCamelCase()}", Type("Context")) {
            body = FunctionBlock(
                FunctionCall(
                    name = "write${template.name.toCamelCase()}",
                    args = template.nonContextParameters.map { it.contextParameterCall() }
                ))
        }
}
