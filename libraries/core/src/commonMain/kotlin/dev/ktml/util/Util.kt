package dev.ktml.util

import dev.ktool.gen.types.Import

internal fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

internal fun String.toPascalCase(): String {
    return toCamelCase().replaceFirstChar { it.lowercase() }
}

internal const val TEMPLATE_PACKAGE = "dev.ktml.templates"

internal data class Path(val path: String) {
    override fun toString() = path
}

internal fun String.toPath() = Path(this)

internal fun String.isVoidTag() = VOID_TAGS.find { it.equals(this, ignoreCase = true) } != null

internal fun String.toImport() = Import(
    packagePath = substringAfter("import ").substringBefore(" as "),
    alias = if (contains(" as ")) substringAfter(" as ") else null
)

private val VOID_TAGS = setOf(
    "br",
    "hr",
    "img",
    "input",
    "meta",
    "link",
    "area",
    "base",
    "col",
    "embed",
    "param",
    "source",
    "track",
    "wbr",
)
