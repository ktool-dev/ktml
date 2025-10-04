package dev.ktml

data class TemplateDefinition(
    val name: String,
    val subPath: String = "",
    val packageName: String,
    val functionName: String,
    val parameters: List<TemplateParameter> = listOf(),
) {
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
