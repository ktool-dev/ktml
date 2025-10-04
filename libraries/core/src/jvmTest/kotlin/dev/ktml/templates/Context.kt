package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

fun Context.writeContext() {
    writeContext(values = required("values"), content = required("content"))
}

fun Context.writeContext(values: Map<String, Any?>, content: Content) {
    copy(values).write(content)
}
