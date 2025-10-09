package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeCompileException() {
    val exception: Throwable = required("exception")
    raw(RAW_CONTENT_0)
    write(exception.message)
    raw(RAW_CONTENT_1)
}

private const val RAW_CONTENT_0: String = """<!DOCTYPE html>
<html class="something"><head>
    <title>KTML Compiler Error</title>
</head><body>
<h1>Compiler Error</h1>
<div style="color: darkred; font-weight: bold; line-height: 20px">
    <pre>"""

private const val RAW_CONTENT_1: String = """</pre>
</div>
</body></html>"""
