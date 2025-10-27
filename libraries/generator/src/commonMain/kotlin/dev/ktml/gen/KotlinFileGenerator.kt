package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.util.replaceTicks
import dev.ktml.util.toCamelCase
import dev.ktool.gen.types.*

class KotlinFileGenerator(private val templates: Templates) {
    fun generateCode(template: ParsedTemplate) = KotlinFile(template.packageName) {
        val content = ContentGenerator(templates, template).generate()

        imports.addAll(content.imports)

        if (template.inRegistry && template.nonContextParameters.isNotEmpty()) {
            generateEmptyParamFunction(template)
        }

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
                    defaultValue =
                        param.defaultValue?.let { ExpressionBody(it.replaceTicks() + param.expression.idComment) }
                }
            }
            body = FunctionBlock(content.body.statements)
        }

    private fun KotlinFile.generateEmptyParamFunction(template: ParsedTemplate) =
        +Function("write${template.name.toCamelCase()}", Type("Context")) {
            +Modifier.Suspend
            body = FunctionBlock {
                write("write${template.name.toCamelCase()}(")
                withIndent {
                    template.nonContextParameters.forEach { newLine(it.contextParameterCall()) }
                }
                newLine(")")
            }
        }
}
