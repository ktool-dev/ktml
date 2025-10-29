package dev.ktml

import dev.ktml.templates.DefaultKtmlRegistry
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@OptIn(ExperimentalJsExport::class)
class KtmlEngine(ktmlRegistry: KtmlRegistry) {
    private val ktmlRegistry = ktmlRegistry.join(DefaultKtmlRegistry)

    suspend fun writePage(context: Context, path: String) {
        ktmlRegistry.templates[path.removePrefix("/")]?.invoke(context) ?: error("No page found for path: $path")
    }
}