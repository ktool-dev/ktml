package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultError() {
    val error: Throwable = required("error")
    raw(TEMPLATE_HTML, 0, 118)
    write(/* start */ error.message /* end: da6630aa-07b0-4538-b98c-d5814523335a */)
    raw(TEMPLATE_HTML, 118, 15)
    write(/* start */ error.stackTraceToString() /* end: 7acd2490-88d0-4991-8e0a-19c4d9e2e005 */)
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
