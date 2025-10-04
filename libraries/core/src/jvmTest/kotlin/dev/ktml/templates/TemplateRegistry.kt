package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.TemplateDefinition
import dev.ktml.TemplateParameter
import dev.ktml.TemplateRegistry
import dev.ktml.templates.basePage.writeHomePage as writeBasePageHomePage
import dev.ktml.templates.elements.writeMyButton as writeElementsMyButton

object TemplateRegistryImpl : TemplateRegistry {
    override val functions: Map<String, Content> = mapOf(
        "if" to { writeIf() },
        "context" to { writeContext() },
        "sidebar" to { writeSidebar() },
        "base-page/home-page" to { writeBasePageHomePage() },
        "card" to { writeCard() },
        "elements/my-button" to { writeElementsMyButton() },
        "dashboard" to { writeDashboard() },
        "test-context" to { writeTestContext() },
        "page-layout" to { writePageLayout() },
        "write-context-value" to { writeWriteContextValue() },
    )

    override val templates: List<TemplateDefinition> = listOf(
        TemplateDefinition(
            name = "if",
            packageName = "dev.ktml.templates",
            functionName = "writeIf",
            parameters = listOf(
                TemplateParameter("test", "Boolean", false),
                TemplateParameter("else", "Content?", true),
                TemplateParameter("content", "Content", false),
            )
        ),
        TemplateDefinition(
            name = "context",
            packageName = "dev.ktml.templates",
            functionName = "writeContext",
            parameters = listOf(
                TemplateParameter("values", "Map<String, Any?>", false),
                TemplateParameter("content", "Content", false),
            )
        ),
        TemplateDefinition(
            name = "sidebar",
            packageName = "dev.ktml.templates",
            functionName = "writeSidebar",
        ),
        TemplateDefinition(
            name = "home-page",
            subPath = "base-page",
            packageName = "dev.ktml.templates.basePage",
            functionName = "writeHomePage",
        ),
        TemplateDefinition(
            name = "card",
            packageName = "dev.ktml.templates",
            functionName = "writeCard",
            parameters = listOf(
                TemplateParameter("header", "Content?", true),
                TemplateParameter("content", "Content", false),
            )
        ),
        TemplateDefinition(
            name = "my-button",
            subPath = "elements",
            packageName = "dev.ktml.templates.elements",
            functionName = "writeMyButton",
            parameters = listOf(
                TemplateParameter("onClick", "String", false),
                TemplateParameter("text", "String", false),
            )
        ),
        TemplateDefinition(
            name = "dashboard",
            packageName = "dev.ktml.templates",
            functionName = "writeDashboard",
        ),
        TemplateDefinition(
            name = "test-context",
            packageName = "dev.ktml.templates",
            functionName = "writeTestContext",
        ),
        TemplateDefinition(
            name = "page-layout",
            packageName = "dev.ktml.templates",
            functionName = "writePageLayout",
            parameters = listOf(
                TemplateParameter("title", "String", true),
                TemplateParameter("header", "Content", false),
                TemplateParameter("content", "Content", false),
            )
        ),
        TemplateDefinition(
            name = "write-context-value",
            packageName = "dev.ktml.templates",
            functionName = "writeWriteContextValue",
        ),
    )

}
