package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.util.replaceTicks
import dev.ktml.util.toCamelCase
import dev.ktool.gen.types.*

class KotlinFileGenerator(templates: Templates) {
    private val contentGenerator = ContentGenerator(templates)

    fun generateCode(template: ParsedTemplate) = KotlinFile(template.packageName) {
        val content = contentGenerator.generateTemplateContent(template)

        imports.addAll(content.imports)

        generateFunction(template, content)

        if (template.externalScriptContent.isNotEmpty()) {
            +Literal(template.externalScriptContent.trim())
        }

        if (content.templateConstantIsNotEmpty) {
            members += content.templateConstant
        }
    }

    private fun KotlinFile.generateFunction(template: ParsedTemplate, content: TemplateContent) =
        +Function("write${template.name.toCamelCase()}", Type("Context")) {
            +Modifier.Suspend
            template.nonContextParameters.map { param ->
                +Parameter(name = param.name, type = Type(param.type)) {
                    defaultValue = param.defaultValue?.let { ExpressionBody(it.replaceTicks()) }
                }
            }
            body = FunctionBlock(content.body.statements)
        }
}
