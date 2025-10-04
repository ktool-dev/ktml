package dev.ktml.gen

import dev.ktml.parser.ParsedTemplate
import dev.ktool.gen.types.*

class TemplateRegistryGenerator(private val templates: List<ParsedTemplate>) {
    /**
     * This will generate an object that implements TemplateInvoker and can invoke a template function by name
     * by pulling any required parameters from the context.
     */
    fun createTemplateRegistry() = KotlinFile("dev.ktml.templates") {
        import("dev.ktml.Content")
        import("dev.ktml.TemplateDefinition")
        import("dev.ktml.TemplateRegistry")
        import("dev.ktml.TemplateParameter")

        templates.filterNot { it.isRootPackage }.forEach { template ->
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
                            newLine(""""${it.fullPath}" to { ${it.uniqueFunctionName}() },""")
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
                                newLine("functionName = \"${template.functionName}\",")
                                if (template.nonContextParameters.isNotEmpty()) {
                                    newLine("parameters = listOf(")
                                    withIndent {
                                        template.nonContextParameters.forEach { param ->
                                            newLine("TemplateParameter(\"${param.name}\", \"${param.type}\", ${param.defaultValue != null}),")
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
