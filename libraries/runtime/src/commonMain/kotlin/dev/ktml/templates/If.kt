package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(if(test) content else `else`)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
