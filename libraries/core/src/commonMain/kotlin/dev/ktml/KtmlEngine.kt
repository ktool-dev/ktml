package dev.ktml

class KtmlEngine(private val templateRegistry: TemplateRegistry) {
    fun writeTemplate(context: Context, path: String) {
        templateRegistry.functions[path]?.invoke(context)
    }
}