package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeDefaultNotFound() {
    val error: Throwable = required("error")
    raw(RAW_CONTENT_0)
}

private const val RAW_CONTENT_0: String = """<!DOCTYPE html>
<html><head>
    <title>Server Error</title>
</head><body>
<div>
    <h1>Resource Not Found</h1>
</div>
</body></html>"""
