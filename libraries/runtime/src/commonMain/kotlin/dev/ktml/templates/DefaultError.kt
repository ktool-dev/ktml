package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultError() {
    val error: Throwable = required("error")
    raw(TEMPLATE_HTML, 0, 108)
    write(error.message)
    raw(TEMPLATE_HTML, 108, 15)
    write(error.stackTraceToString())
    raw(TEMPLATE_HTML, 123, 28)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html><head>
    <title>Server Error</title>
</head><body>
<div>
    <h1>Error</h1>
    <h3></h3>
    <pre></pre>
</div>
</body></html>"""
