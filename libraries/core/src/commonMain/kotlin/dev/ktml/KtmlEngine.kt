package dev.ktml

import dev.ktml.templates.DefaultKtmlRegistry

class KtmlEngine(ktmlRegistry: KtmlRegistry) {
    private val ktmlRegistry = ktmlRegistry.join(DefaultKtmlRegistry)

    suspend fun writePage(context: Context, path: String) {
        ktmlRegistry.templates[path]?.invoke(context) ?: error("No page found for path: $path")
    }
}