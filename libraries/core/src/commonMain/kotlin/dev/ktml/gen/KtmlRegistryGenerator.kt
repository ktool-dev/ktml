package dev.ktml.gen

import dev.ktml.parser.Templates
import dev.ktool.gen.types.*

object KtmlRegistryGenerator {
    /**
     * This will generate an object that implements the KtmlRegistry interface that can be used to look up templates.
     */
    fun createKtmlRegistry(basePackageName: String, templates: Templates) =
        KotlinFile(basePackageName) {
            import("dev.ktml.Content")
            import("dev.ktml.TagDefinition")
            import("dev.ktml.KtmlRegistry")
            import("dev.ktml.TagParameter")

            templates.allPages.filter { it.subPath.isNotEmpty() }.forEach { template ->
                import(template.qualifiedFunctionName, template.uniqueFunctionName)
            }

            obj("KtmlRegistryImpl") {
                superType("KtmlRegistry")

                property(name = "pages", type = Type("Map", typeArguments = listOf(StringType, Type("Content")))) {
                    modifier(Modifier.Override)
                    initializer = ExpressionBody {
                        write("mapOf(")
                        withIndent {
                            templates.allPages.forEach {
                                newLine(""""${it.path}" to { ${it.uniqueFunctionName}() },""")
                            }
                        }
                        newLine(")")
                    }
                }

                property(name = "tags", type = Type("List", typeArguments = listOf(Type("TagDefinition")))) {
                    modifier(Modifier.Override)
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
