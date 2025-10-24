package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(/* start */ if (test) write(content) else write(`else`) /* end: 5000e2a9-88a6-4660-8199-6f0b111daa1a */)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
