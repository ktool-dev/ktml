package dev.ktml.util

import dev.ktool.gen.types.Import

fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

fun String.toPascalCase(): String {
    return toCamelCase().replaceFirstChar { it.lowercase() }
}

fun String.toKebabCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()
}

internal data class Path(val path: String) {
    override fun toString() = path
}

internal fun String.toPath() = Path(this)

fun String.isVoidTag() = VOID_TAGS.find { it.equals(this, ignoreCase = true) } != null

fun String.requiresCloseTag() = TAGS_REQUIRING_CLOSE.find { it.equals(this, ignoreCase = true) } != null

fun String.toImport() = Import(
    packagePath = substringAfter("import ").substringBefore(" as "),
    alias = if (contains(" as ")) substringAfter(" as ") else null
)

const val ROOT_PACKAGE = "dev.ktml.templates"
const val ROOT_PACKAGE_PATH = "dev/ktml/templates"

private val TAGS_REQUIRING_CLOSE = setOf("script")

private val VOID_TAGS = setOf(
    "br", "hr", "img", "input", "meta", "link", "area", "base", "col", "embed", "param", "source", "track", "wbr",
)
