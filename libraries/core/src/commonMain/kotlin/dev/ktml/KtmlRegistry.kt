package dev.ktml

data class TagDefinition(
    val name: String,
    val subPath: String = "",
    val packageName: String,
    val parameters: List<TagParameter> = listOf(),
) {
    val functionName = "write${name.toCamelCase()}"
    val path = if (subPath.isEmpty()) name else "$subPath/$name"
    val qualifiedFunctionName = "$packageName.$functionName"
    val uniqueFunctionName = "write" + subPath.replace("/", "-").toCamelCase() + functionName.substringAfter("write")
}

data class TagParameter(
    val name: String,
    val type: String,
    val hasDefault: Boolean,
) {
    val isContent = type == "Content" || type == "Content?"
    val isString = type == "String" || type == "String?"
    val isNullable = type.endsWith("?")
}

interface KtmlRegistry {
    val templates: Map<String, Content>
    val tags: List<TagDefinition>

    fun hasPath(path: String): Boolean = templates.containsKey(path)
    fun hasTag(path: String): Boolean = tags.any { it.path == path }
}

fun KtmlRegistry.join(vararg registries: KtmlRegistry) = KtmlRegistryList(listOf(this) + registries)

class KtmlRegistryList(private val registries: List<KtmlRegistry>) : KtmlRegistry {
    override val templates: Map<String, Content>
        get() = buildMap { registries.forEach { putAll(it.templates) } }
    override val tags: List<TagDefinition>
        get() = registries.flatMap { it.tags }
}

private fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
