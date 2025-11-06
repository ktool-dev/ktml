package dev.ktml.gen

import dev.ktml.parser.Templates
import dev.ktool.gen.types.*

/**
 * This will generate an object that implements the KtmlRegistry interface that can be used to look up templates.
 */
fun createKtmlRegistry(basePackageName: String, templates: Templates) = KotlinFile(basePackageName) {
    +Import("dev.ktml.Content")
    +Import("dev.ktml.TagDefinition")
    +Import("dev.ktml.KtmlRegistry")
    +Import("dev.ktml.TagParameter")

    templates.registryTemplates.filter { it.subPath.isNotEmpty() }.forEach { template ->
        +Import(template.qualifiedFunctionName, template.uniqueFunctionName)
    }

    +Object("KtmlRegistry") {
        +SuperType("KtmlRegistry")

        +Function(name = "get", returnType = Type("Content?")) {
            +Modifier.Override
            +Modifier.Operator
            
            +Parameter(name = "path", type = StringType)
            body = ExpressionBody("templates[path]")
        }

        +Property(
            name = "templates",
            type = Type("Map", typeArguments = listOf(TypeArgument("String"), TypeArgument("Content")))
        ) {
            +Modifier.Private
            initializer = ExpressionBody {
                write("mapOf(")
                withIndent {
                    templates.registryTemplates.forEach {
                        newLine(""""${it.path}" to { ${it.uniqueFunctionName}() },""")
                        newLine()
                    }
                }
                trimEnd()
                newLine(")")
            }
        }

        +Property(name = "tags", type = Type("List", typeArguments = listOf(TypeArgument("TagDefinition")))) {
            +Modifier.Override
            initializer = ExpressionBody {
                write("listOf(")
                withIndent {
                    templates.allTags.forEach { tag ->
                        newLine("TagDefinition(")
                        withIndent {
                            newLine("name = \"${tag.name}\",")
                            if (tag.subPath.isNotEmpty()) {
                                newLine("subPath = \"${tag.subPath}\",")
                            }
                            newLine("packageName = \"${tag.packageName}\",")
                            if (tag.parameters.isNotEmpty()) {
                                newLine("parameters = listOf(")
                                withIndent {
                                    tag.parameters.forEach { param ->
                                        newLine("TagParameter(\"${param.name}\", \"${param.type}\", ${param.hasDefault}),")
                                    }
                                }
                                newLine(")")
                            }
                        }
                        newLine("),")
                    }
                }
                newLine(")")
            }
        }
    }
}.render()
