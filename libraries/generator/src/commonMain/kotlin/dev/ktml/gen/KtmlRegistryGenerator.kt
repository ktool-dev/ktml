package dev.ktml.gen

import dev.ktml.parser.Templates
import dev.ktool.gen.types.*

object KtmlRegistryGenerator {
    /**
     * This will generate an object that implements the KtmlRegistry interface that can be used to look up templates.
     */
    fun createKtmlRegistry(basePackageName: String, templates: Templates) =
        KotlinFile(basePackageName) {
            addImport("dev.ktml.Content")
            addImport("dev.ktml.TagDefinition")
            addImport("dev.ktml.KtmlRegistry")
            addImport("dev.ktml.TagParameter")

            templates.registryTemplates.filter { it.subPath.isNotEmpty() }.forEach { template ->
                addImport(template.qualifiedFunctionName, template.uniqueFunctionName)
            }

            addObject("KtmlRegistryImpl") {
                addSuperType("KtmlRegistry")

                addValProperty(
                    name = "templates",
                    type = Type("Map", typeArguments = listOf(StringType, Type("Content")))
                ) {
                    addModifiers(Modifier.Override)
                    initializer = ExpressionBody {
                        write("mapOf(")
                        withIndent {
                            templates.registryTemplates.forEach {
                                val hasParameters = it.nonContextParameters.isNotEmpty()

                                newLine(""""${it.path}" to { """)

                                if (hasParameters) {
                                    indent()
                                    newLine()
                                }

                                write("${it.uniqueFunctionName}(")

                                if (it.nonContextParameters.isNotEmpty()) {
                                    withIndent {
                                        it.nonContextParameters.forEach { parameter ->
                                            newLine(parameter.contextParameterCall())
                                            write(",")
                                        }
                                    }
                                    newLine()
                                }
                                write(") ")
                                if (hasParameters) {
                                    unindent()
                                    newLine()
                                }
                                write("},")
                                newLine()
                            }
                        }
                        trimEnd()
                        newLine(")")
                    }
                }

                addValProperty(name = "tags", type = Type("List", typeArguments = listOf(Type("TagDefinition")))) {
                    addModifiers(Modifier.Override)
                    initializer = ExpressionBody {
                        write("listOf(")
                        withIndent {
                            templates.allTags.forEach { template ->
                                newLine("TagDefinition(")
                                withIndent {
                                    newLine("name = \"${template.name}\",")
                                    if (template.subPath.isNotEmpty()) {
                                        newLine("subPath = \"${template.subPath}\",")
                                    }
                                    newLine("packageName = \"${template.packageName}\",")
                                    if (template.parameters.isNotEmpty()) {
                                        newLine("parameters = listOf(")
                                        withIndent {
                                            template.parameters.forEach { param ->
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
}
