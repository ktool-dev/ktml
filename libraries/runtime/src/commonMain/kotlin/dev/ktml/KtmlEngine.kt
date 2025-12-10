package dev.ktml

import dev.ktml.templates.DefaultKtmlRegistry

class KtmlEngine(ktmlRegistry: KtmlRegistry) {
    private val ktmlRegistry: KtmlRegistry = ktmlRegistry.join(DefaultKtmlRegistry)

    private fun lookup(path: String): Content? = ktmlRegistry[path.removePrefix("/")]

    suspend fun writePage(context: Context, path: String) {
        lookup(path)?.invoke(context) ?: error("No page found for path: $path")
    }

    suspend fun renderPage(path: String, model: Map<String, Any> = mapOf()) = StringContentWriter().also {
        writePage(Context(it, model), path)
    }.toString()

    fun hasPage(path: String): Boolean = lookup(path) != null
}