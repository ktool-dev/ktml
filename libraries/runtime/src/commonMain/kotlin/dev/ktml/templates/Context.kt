package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeContext(values: Map<String, Any?>, content: Content) {
    copy(values).write(content)
}
