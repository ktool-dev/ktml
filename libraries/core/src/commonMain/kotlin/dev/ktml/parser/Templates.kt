package dev.ktml.parser

import dev.ktml.KtmlRegistry
import dev.ktml.KtmlRegistryList
import dev.ktml.TagDefinition
import dev.ktml.TagParameter
import dev.ktml.templates.DefaultKtmlRegistry

/**
 * Registry for managing template dependencies and composition
 */
class Templates(ktmlRegistries: List<KtmlRegistry> = listOf()) {
    private val tags = mutableMapOf<String, TagDefinition>()
    private val pages = mutableMapOf<String, ParsedTemplate>()
    private val ktmlRegistry: KtmlRegistry = KtmlRegistryList(ktmlRegistries.plus(DefaultKtmlRegistry))

    fun clear() {
        tags.clear()
        pages.clear()
    }

    val allTags: Collection<TagDefinition> get() = tags.values
    val allPages: Collection<ParsedTemplate> get() = pages.values

    fun register(template: TagDefinition) {
        require(!tags.containsKey(template.name)) { "Tag '${template.path}' already registered" }

        tags[template.path] = template
    }

    fun register(template: ParsedTemplate) {
        if (template.isPage) {
            require(!pages.containsKey(template.path)) { "The directory '${template.subPath}' already has a page defined in it. You can only define one page for a directory." }

            pages[template.path] = template
        } else {
            register(template.toTagDefinition())
        }
    }

    fun remove(template: ParsedTemplate) {
        if (template.isPage) {
            pages.remove(template.path)
        } else {
            tags.remove(template.path)
        }
    }

    fun replace(tag: TagDefinition) {
        tags[tag.path] = tag
    }

    fun replace(template: ParsedTemplate) {
        if (template.isPage) {
            pages[template.path] = template
        } else {
            replace(template.toTagDefinition())
        }
    }

    operator fun get(path: String): TagDefinition? = tags[path]

    fun locate(referencePath: String, name: String): TagDefinition? {
        val tags = allTags + ktmlRegistry.tags
        val allMatches = tags.filter { it.name == name }

        if (allMatches.size < 2) return allMatches.firstOrNull()

        val sameDirectory = allMatches.firstOrNull { it.subPath == referencePath }
        if (sameDirectory != null) return sameDirectory

        val subTemplates = allMatches.filter { it.subPath.startsWith(referencePath) }
        if (subTemplates.size == 1) return subTemplates.first()

        val parentTemplates = allMatches.filter { referencePath.startsWith(it.subPath) }
        if (parentTemplates.size == 1) return parentTemplates.first()

        error(
            """
            Cannot resolve tag because multiple tags were found with the name: $name
            A tag can be resolved if only one exists with that name, or if only one exists in the same folder
            or a sub-folder as the current template, or if only one exists in a parent folder of the current template.
            
            The following tags were found:
            ${allMatches.joinToString("\n") { " - ${it.path}" }}
        """.trimIndent()
        )
    }
}

private fun ParsedTemplate.toTagDefinition() = TagDefinition(
    name = name,
    subPath = subPath,
    packageName = packageName,
    parameters = nonContextParameters.map { TagParameter(it.name, it.type, it.defaultValue != null) }
)