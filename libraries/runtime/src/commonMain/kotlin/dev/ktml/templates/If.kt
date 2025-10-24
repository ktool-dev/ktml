package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(/* start */ if (test) write(content) else write(`else`) /* end: 874ac1ff-1842-4410-9bf0-da2249971aa7 */)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
