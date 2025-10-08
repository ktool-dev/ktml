package dev.ktml

class KtmlEngine(private val templateRegistry: KtmlRegistry) {
    suspend fun writePage(context: Context, path: String) {
        templateRegistry.pages[path]?.invoke(context) ?: error("No page found for path: $path")
    }
}