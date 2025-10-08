package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultError() {
    val error: Throwable = required("error")
    raw(RAW_CONTENT_0)
    write(error.message)
    raw(RAW_CONTENT_1)
    write(error.stackTraceToString())
    raw(RAW_CONTENT_2)
}

private const val RAW_CONTENT_0: String = """<!DOCTYPE html>
<html><head>
    <title>Server Error</title>
</head><body>
<div>
    <h1>Error</h1>
    <h3>"""

private const val RAW_CONTENT_1: String = """</h3>
    <pre>"""

private const val RAW_CONTENT_2: String = """</pre>
</div>
</body></html>"""
