package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultError() {
    val error: Throwable = required("error")
    raw(TEMPLATE_HTML, 0, 118)
    write(/* start */ error.message /* end: 9d3e3f63-0711-4efd-b8c3-4739e81b7b1e */)
    raw(TEMPLATE_HTML, 118, 15)
    write(/* start */ error.stackTraceToString() /* end: 077bc4ae-9dbe-42f0-96f8-5ad71e19b864 */)
    raw(TEMPLATE_HTML, 133, 28)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html lang="en"><head>
    <title>Server Error</title>
</head><body>
<div>
    <h1>Error</h1>
    <h3></h3>
    <pre></pre>
</div>
</body></html>"""
