package dev.ktml.templates.elements

import dev.ktml.Context

fun Context.writeMyButton(
    text: String,
    onClick: String,
) {
    raw(RAW_CONTENT_0)
    write(onClick)
    raw(RAW_CONTENT_1)
    write(text)
    raw(RAW_CONTENT_2)
}

private const val RAW_CONTENT_0 = """<button onclick=""""
private const val RAW_CONTENT_1 = """">"""
private const val RAW_CONTENT_2 = """</button>"""
