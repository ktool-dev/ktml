package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultNotFound() {
    raw(TEMPLATE_HTML, 0, 131)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html><head>
    <title>Not Found</title>
</head><body>
<div>
    <h1>Resource Not Found</h1>
</div>
</body></html>"""
