package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

suspend fun Context.writeIf(test: Boolean, `else`: Content? = null /*id:8ac67d55-5f8c-41e3-9dde-0714aa898a81*/, content: Content) {
    raw(TEMPLATE_HTML, 0, 5)
    write(if (test) write(content) else write(`else`) /*id:887b2a04-c5f1-432a-9c73-a63e0ca13047*/)
    raw(TEMPLATE_HTML, 5, 1)
}

private const val TEMPLATE_HTML: String = """
    
"""
