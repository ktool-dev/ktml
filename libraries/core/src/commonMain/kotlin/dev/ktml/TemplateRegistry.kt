package dev.ktml

import dev.ktml.util.toCamelCase

data class TemplateDefinition(
    val name: String,
    val subPath: String = "",
    val packageName: String,
    val parameters: List<TemplateParameter> = listOf(),
) {
    val functionName = "write${name.toCamelCase()}"
    val path = if (subPath.isEmpty()) name else "$subPath/$name"
    val qualifiedFunctionName = "$packageName.$functionName"
}

data class TemplateParameter(
    val name: String,
    val type: String,
    val hasDefault: Boolean,
) {
    val isContent = type == "Content" || type == "Content?"
    val isString = type == "String" || type == "String?"
    val isNullable = type.endsWith("?")
}

interface TemplateRegistry {
    val functions: Map<String, Content>
    val templates: List<TemplateDefinition>
}
