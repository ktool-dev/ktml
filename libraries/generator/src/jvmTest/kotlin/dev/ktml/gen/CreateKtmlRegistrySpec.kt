package dev.ktml.gen

import dev.ktml.DEFAULT_PACKAGE
import dev.ktml.parsedTemplateParameter
import dev.ktml.parser.*
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.string.shouldContain

private var templates = Templates()
private const val basePackageName = "dev.ktml.templates"

class KtmlRegistryGeneratorSpec : BddSpec({
    beforeEach {
        templates = Templates()
    }

    "generate registry with single tag" {
        Given
        template(
            name = "button",
            parameters = listOf(parsedTemplateParameter("text", "String")),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "object KtmlRegistry : KtmlRegistry"
        result shouldContain """
        TagDefinition(
            name = "button",
            packageName = "dev.ktml.templates",
            parameters = listOf(
                TagParameter("text", "String", false),
            )
        ),
        """.trim()
    }

    "generate registry with multiple templates" {
        Given
        template(
            name = "button",
            parameters = listOf(parsedTemplateParameter("text", "String")),
        )
        template(
            name = "card",
            parameters = listOf(
                parsedTemplateParameter("title", "String"),
                parsedTemplateParameter("content", "Content"),
            ),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain """
    override val tags: List<TagDefinition> = listOf(
        TagDefinition(
            name = "button",
            packageName = "dev.ktml.templates",
            parameters = listOf(
                TagParameter("text", "String", false),
            )
        ),
        TagDefinition(
            name = "card",
            packageName = "dev.ktml.templates",
            parameters = listOf(
                TagParameter("title", "String", false),
                TagParameter("content", "Content", false),
            )
        ),
    )
        """.trim()
    }

    "generate registry with template with default values" {
        Given
        template(
            name = "button",
            parameters = listOf(
                parsedTemplateParameter("text", "String", "\"blah\""),
                parsedTemplateParameter("disabled", "Boolean", "true")
            ),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "TagParameter(\"text\", \"String\", true)"
        result shouldContain "TagParameter(\"disabled\", \"Boolean\", true)"
    }

    "generate registry with template in subpath" {
        Given
        template(
            inRegistry = true,
            name = "my-page",
            subPath = "components",
            parameters = listOf(parsedTemplateParameter("@name", "String")),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "import dev.ktml.templates.components.writeMyPage as writeComponentsMyPage"
        result shouldContain """
    private val templates: Map<String, Content> = mapOf(
        "components/my-page" to { writeComponentsMyPage() },
    )

    override val paths: List<String> = templates.keys.toList()

    override operator fun get(path: String): Content? = templates[path]
        """.trim()
    }

    "generate registry with template without parameters" {
        Given
        template(
            inRegistry = true,
            name = "header",
            parameters = emptyList(),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "\"header\" to { writeHeader() }"
    }

    "generate registry imports" {
        Given
        template(
            name = "button",
            parameters = emptyList(),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "import dev.ktml.Content"
        result shouldContain "import dev.ktml.TagDefinition"
        result shouldContain "import dev.ktml.KtmlRegistry"
        result shouldContain "import dev.ktml.TagParameter"
    }

    "generate registry with mixed subpaths" {
        Given
        template(
            inRegistry = true,
            name = "button",
            parameters = emptyList(),
        )
        template(
            inRegistry = true,
            name = "icon",
            subPath = "components",
            parameters = emptyList(),
        )

        When
        val result = createKtmlRegistry(basePackageName, templates)

        Then
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "import dev.ktml.templates.components.writeIcon as writeComponentsIcon"
        result shouldContain "\"components/icon\" to { writeComponentsIcon() }"
    }
})

private fun template(
    name: String = "test",
    subPath: String = "",
    inRegistry: Boolean = false,
    children: MutableList<HtmlElement> = mutableListOf(),
    imports: List<String> = listOf(),
    parameters: List<ParsedTemplateParameter> = listOf(),
) = ParsedTemplate(
    file = "testFile",
    name = name,
    inRegistry = inRegistry,
    subPath = subPath,
    imports = imports,
    parameters = parameters,
    expressions = listOf(),
    templatePackage = DEFAULT_PACKAGE,
    root = HtmlTag("root", emptyMap(), children),
).also { templates.register(it) }
