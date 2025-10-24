package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultNotFound() {
    raw(TEMPLATE_HTML, 0, 141)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html lang="en"><head>
    <title>Not Found</title>
</head><body>
<div>
    <h1>Resource Not Found</h1>
</div>
</body></html>"""
