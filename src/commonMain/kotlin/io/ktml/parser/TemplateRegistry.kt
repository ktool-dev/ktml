package io.ktml.parser

/**
 * Registry for managing template dependencies and composition
 */
class TemplateRegistry {
    private val templates = mutableMapOf<String, ParsedTemplate>()
    private val dependencies = mutableMapOf<String, Set<String>>()

    /**
     * Register a parsed template
     */
    fun registerTemplate(template: ParsedTemplate) {
        templates[template.name] = template
        dependencies[template.name] = findTemplateDependencies(template)
    }

    /**
     * Get a template by name
     */
    fun getTemplate(name: String): ParsedTemplate? {
        return templates[name]
    }

    /**
     * Get all registered templates
     */
    fun getAllTemplates(): Collection<ParsedTemplate> {
        return templates.values
    }

    /**
     * Get templates in dependency order (dependencies first)
     */
    fun getTemplatesInDependencyOrder(): List<ParsedTemplate> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<ParsedTemplate>()

        fun visit(templateName: String) {
            if (templateName in visited) return
            visited.add(templateName)

            // Visit dependencies first
            dependencies[templateName]?.forEach { dependency ->
                if (dependency in templates) {
                    visit(dependency)
                }
            }

            // Add current template
            templates[templateName]?.let { result.add(it) }
        }

        templates.keys.forEach { visit(it) }
        return result
    }

    /**
     * Find all template dependencies for a given template
     */
    private fun findTemplateDependencies(template: ParsedTemplate): Set<String> {
        val dependencies = mutableSetOf<String>()
        findDependenciesInElement(template.root, dependencies)
        return dependencies
    }

    /**
     * Recursively find dependencies in an HTML element
     */
    private fun findDependenciesInElement(element: HtmlElement, dependencies: MutableSet<String>) {
        when (element) {
            is HtmlElement.Tag -> {
                // Check if this tag represents a template dependency
                if (isCustomTemplateTag(element.name)) {
                    dependencies.add(element.name)
                }

                // Recursively check children
                element.children.forEach { child ->
                    findDependenciesInElement(child, dependencies)
                }
            }

            is HtmlElement.Text -> {
                // Text nodes don't have dependencies
            }
        }
    }

    /**
     * Check if a tag name represents a custom template
     */
    private fun isCustomTemplateTag(tagName: String): Boolean {
        // Custom template tags are either:
        // 1. Kebab-case names (contain hyphens)
        // 2. Known template names
        return tagName.contains("-") ||
                tagName in setOf("card", "sidebar", "page-layout", "dashboard") ||
                tagName in templates.keys
    }

    /**
     * Validate that all template dependencies are satisfied
     */
    fun validateDependencies(): List<String> {
        val errors = mutableListOf<String>()

        dependencies.forEach { (templateName, deps) ->
            deps.forEach { dependency ->
                if (dependency !in templates) {
                    errors.add("Template '$templateName' depends on '$dependency' which is not registered")
                }
            }
        }

        return errors
    }

    /**
     * Check for circular dependencies
     */
    fun checkCircularDependencies(): List<String> {
        val errors = mutableListOf<String>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        fun visit(templateName: String, path: List<String>) {
            if (templateName in visiting) {
                val cycle = path.dropWhile { it != templateName } + templateName
                errors.add("Circular dependency detected: ${cycle.joinToString(" -> ")}")
                return
            }

            if (templateName in visited) return

            visiting.add(templateName)

            dependencies[templateName]?.forEach { dependency ->
                if (dependency in templates) {
                    visit(dependency, path + templateName)
                }
            }

            visiting.remove(templateName)
            visited.add(templateName)
        }

        templates.keys.forEach { templateName ->
            if (templateName !in visited) {
                visit(templateName, emptyList())
            }
        }

        return errors
    }
}
