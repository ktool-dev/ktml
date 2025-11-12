package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktml.TagParameter

object DefaultKtmlRegistry : KtmlRegistry {
    private val templates: Map<String, Content> = mapOf(
        "compile-exception" to { writeCompileException() },
    )

    override val paths: List<String> = templates.keys.toList()

    override operator fun get(path: String): Content? = templates[path]

    override val tags: List<TagDefinition> = listOf(
        TagDefinition(
            name = "if",
            packageName = "dev.ktml.templates",
            parameters = listOf(
                TagParameter("test", "Boolean", false),
                TagParameter("else", "Content?", true),
                TagParameter("content", "Content", false),
            )
        ),
    )

}
