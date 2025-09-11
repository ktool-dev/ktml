package io.ktml

internal fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

internal const val TEMPLATE_PACKAGE = "io.ktml.templates"
internal const val INDENTATION = "    "
internal const val TRIPLE_QUOTE = "\"\"\""

internal data class Path(val path: String) {
    override fun toString() = path
}

internal fun String.toPath() = Path(this)
