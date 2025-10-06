package dev.ktml.parser

import dev.ktml.TemplateDefinition

/**
 * Registry for managing template dependencies and composition
 */
class TemplateDefinitions {
    private val templates = mutableMapOf<String, TemplateDefinition>()

    val all: Collection<TemplateDefinition> get() = templates.map { it.value }

    fun register(template: TemplateDefinition) {
        require(!templates.containsKey(template.name)) { "Template '${template.path}' already registered" }

        templates[template.path] = template
    }

    fun replace(template: TemplateDefinition) {
        templates[template.path] = template
    }

    operator fun get(path: String): TemplateDefinition? = templates[path]

    fun locate(referencePath: String, name: String): TemplateDefinition? {
        val allMatches = all.filter { it.name == name }

        if (allMatches.size < 2) return allMatches.firstOrNull()

        val sameDirectory = allMatches.firstOrNull { it.subPath == referencePath }
        if (sameDirectory != null) return sameDirectory

        val subTemplates = allMatches.filter { it.subPath.startsWith(referencePath) }
        if (subTemplates.size == 1) return subTemplates.first()

        val parentTemplates = allMatches.filter { referencePath.startsWith(it.subPath) }
        if (parentTemplates.size == 1) return parentTemplates.first()

        error(
            """
            Cannot resolve template because multiple templates were found with the name: $name
            A template can be resolved if only one exists with that name, or if only one exists in the same folder
            or a sub-folder as the current template, or if only one exists in a parent folder of the current template.
            
            The following templates were found:
            ${allMatches.joinToString("\n") { " - ${it.path}" }}
        """.trimIndent()
        )
    }
}