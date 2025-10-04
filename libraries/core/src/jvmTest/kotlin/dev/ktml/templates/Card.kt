package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

fun Context.writeCard() {
    writeCard(header = optionalNullable("header", null as Content?), content = required("content"))
}

fun Context.writeCard(header: Content? = null, content: Content) {
    raw(RAW_CONTENT_0)
    if (header != null) {
        raw(RAW_CONTENT_1)
        write(header)
        raw(RAW_CONTENT_2)
    }
    raw(RAW_CONTENT_3)
    write(content)
    raw(RAW_CONTENT_4)
}

private const val RAW_CONTENT_0: String = """<div class="card">
        """

private const val RAW_CONTENT_1: String = """<div class="card-header">
            """

private const val RAW_CONTENT_2: String = """
        </div>"""

private const val RAW_CONTENT_3: String = """
        <div class="card-body">
            """

private const val RAW_CONTENT_4: String = """
        </div>
    </div>"""
