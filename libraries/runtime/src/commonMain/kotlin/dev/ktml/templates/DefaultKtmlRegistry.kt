package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktml.TagParameter

object DefaultKtmlRegistry : KtmlRegistry {
    override val templates: Map<String, Content> = mapOf(
        "default-not-found" to { writeDefaultNotFound() },

        "compile-exception" to { writeCompileException() },

        "default-error" to { writeDefaultError() },
    )

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
        TagDefinition(
            name = "context",
            packageName = "dev.ktml.templates",
            parameters = listOf(
                TagParameter("values", "Map<String, Any?>", false),
                TagParameter("content", "Content", false),
            )
        ),
    )

}
