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
    operator fun get(path: String): Content?
    val tags: List<TagDefinition>
    val paths: List<String>

    fun hasPath(path: String): Boolean = path.contains(path)
    fun hasTag(path: String): Boolean = tags.any { it.path == path }
    fun toKtmlEngine() = KtmlEngine(this)
}

fun List<KtmlRegistry>.merge(): KtmlRegistry {
    require(isNotEmpty()) { "Cannot merge empty entries" }
    if (size == 1) return this.first()

    return subList(1, size).foldRight(first(), ::DualKtmlRegistry)
}

fun KtmlRegistry.join(registry: KtmlRegistry) = DualKtmlRegistry(this, registry)

class DualKtmlRegistry(private val first: KtmlRegistry, private val second: KtmlRegistry) : KtmlRegistry {
    override operator fun get(path: String): Content? = first[path] ?: second[path]

    override val tags: List<TagDefinition> get() = first.tags + second.tags
    override val paths: List<String> get() = first.paths + second.paths
}

private fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
