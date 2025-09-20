package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

fun Context.writeCard(
    body: Content,
    header: Content,
) {
    raw(RAW_CONTENT_0)
    write(header)
    raw(RAW_CONTENT_1)
    write(body)
    raw(RAW_CONTENT_2)
}

private const val RAW_CONTENT_0 = """<div class="card">
        <div class="card-header">
            """
private const val RAW_CONTENT_1 = """
        </div>
        <div class="card-body">
            """
private const val RAW_CONTENT_2 = """
        </div>
    </div>"""
