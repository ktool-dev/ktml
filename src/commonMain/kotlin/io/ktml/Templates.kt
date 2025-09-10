package io.ktml

import io.ktml.parser.ParsedTemplate

/**
 * Registry for managing template dependencies and composition
 */
class Templates {
    private val templates = mutableMapOf<String, ParsedTemplate>()

    val all: Collection<ParsedTemplate> get() = templates.map { it.value }

    fun register(template: ParsedTemplate) {
        require(!templates.containsKey(template.name)) { "Template '${template.key}' already registered" }

        templates[template.key] = template
    }

    fun replace(template: ParsedTemplate) {
        templates[template.key] = template
    }

    fun locate(name: String, template: ParsedTemplate): ParsedTemplate? {
        val allMatches = all.filter { it.name == name }

        if (allMatches.size < 2) return allMatches.firstOrNull()

        val sameDirectory = allMatches.firstOrNull { it.packageName == template.packageName }
        if (sameDirectory != null) return sameDirectory

        val subTemplates = allMatches.filter { it.packageName.startsWith(template.packageName) }
        if (subTemplates.size == 1) return subTemplates.first()

        val parentTemplates = allMatches.filter { template.packageName.startsWith(it.packageName) }
        if (parentTemplates.size == 1) return parentTemplates.first()

        error(
            """
            Cannot resolve template because multiple templates were found with the name: $name
            A template can be resolved if only one exists with that name, or if only one exists in the same folder
            or a sub-folder as the current template, or if only one exists in a parent folder of the current template.
            
            The following templates were found:
            ${allMatches.joinToString("\n") { " - ${it.key}" }}
        """.trimIndent()
        )
    }
}