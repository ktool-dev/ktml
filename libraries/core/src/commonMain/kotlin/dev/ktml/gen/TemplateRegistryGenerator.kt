package dev.ktml.gen

import dev.ktml.TemplateDefinition
import dev.ktml.util.toCamelCase
import dev.ktool.gen.types.*

object TemplateRegistryGenerator {
    /**
     * This will generate an object that implements the TemplateRegistry interface that can be used to look up templates.
     */
    fun createTemplateRegistry(basePackageName: String, templates: List<TemplateDefinition>) =
        KotlinFile(basePackageName) {
            import("dev.ktml.Content")
            import("dev.ktml.TemplateDefinition")
            import("dev.ktml.TemplateRegistry")
            import("dev.ktml.TemplateParameter")

            templates.filter { it.subPath.isNotEmpty() }.forEach { template ->
                import(template.qualifiedFunctionName, template.uniqueFunctionName)
            }

            obj("TemplateRegistryImpl") {
                superType("TemplateRegistry")

                property(name = "functions", type = Type("Map", typeArguments = listOf(StringType, Type("Content")))) {
                    modifier(Modifier.Override)
                    initializer = ExpressionBody {
                        write("mapOf(")
                        withIndent {
                            templates.forEach {
                                newLine(""""${it.path}" to { ${it.uniqueFunctionName}() },""")
                            }
                        }
                        newLine(")")
                    }
                }

                property(name = "templates", type = Type("List", typeArguments = listOf(Type("TemplateDefinition")))) {
                    modifier(Modifier.Override)
                    initializer = ExpressionBody {
                        write("listOf(")
                        withIndent {
                            templates.forEach { template ->
                                newLine("TemplateDefinition(")
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
                                                newLine("TemplateParameter(\"${param.name}\", \"${param.type}\", ${param.hasDefault}),")
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

private val TemplateDefinition.uniqueFunctionName: String
    get() = if (subPath.isEmpty()) functionName else
        "write" + subPath.replace("/", "-").toCamelCase() + functionName.substringAfter("write")
