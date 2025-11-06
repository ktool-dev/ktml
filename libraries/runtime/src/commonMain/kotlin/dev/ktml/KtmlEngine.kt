package dev.ktml

import dev.ktml.templates.DefaultKtmlRegistry

class KtmlEngine(ktmlRegistry: KtmlRegistry) {
    private val ktmlRegistry = ktmlRegistry.join(DefaultKtmlRegistry)

    suspend fun writePage(context: Context, path: String) {
        ktmlRegistry[path.removePrefix("/")]?.invoke(context) ?: error("No page found for path: $path")
    }

    suspend fun renderPage(path: String, model: Map<String, Any> = mapOf()) = StringContentWriter().also {
        writePage(Context(it, model), path)
    }.toString()
}