package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeCompileException() {
    val exception: Throwable = required("exception")
    raw(TEMPLATE_HTML, 0, 200)
    write(exception.message)
    raw(TEMPLATE_HTML, 200, 28)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html class="something"><head>
    <title>KTML Compiler Error</title>
</head><body>
<h1>Compiler Error</h1>
<div style="color: darkred; font-weight: bold; line-height: 20px">
    <pre></pre>
</div>
</body></html>"""
