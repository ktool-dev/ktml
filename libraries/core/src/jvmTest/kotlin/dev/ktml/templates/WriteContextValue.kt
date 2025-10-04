package dev.ktml.templates

import dev.ktml.Context

fun Context.writeWriteContextValue() {
    val value: String = required("value")
    raw(RAW_CONTENT_0)
    write(value)
    raw(RAW_CONTENT_1)
}

private const val RAW_CONTENT_0: String = """<div>"""

private const val RAW_CONTENT_1: String = """</div>"""
