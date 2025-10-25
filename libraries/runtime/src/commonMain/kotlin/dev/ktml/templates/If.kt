package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null /*id:24c87024-9258-4339-b888-7045ae069599*/, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(if (test) write(content) else write(`else`) /*id:ffa5af0f-9e02-4dba-86b5-4ba30789a924*/)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
