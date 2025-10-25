package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null /*id:a6408305-2372-4c98-bff6-d74629de0e3f*/, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(if (test) write(content) else write(`else`) /*id:d4f39e23-6204-46a1-be75-471c8c7a806e*/)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
