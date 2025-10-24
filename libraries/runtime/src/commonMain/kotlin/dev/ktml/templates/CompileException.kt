package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeCompileException() {
    val exception: Throwable = required("exception")
    raw(TEMPLATE_HTML, 0, 192)
    write(/* start */ exception.message /* end: aae42d89-8f2d-41f2-afc5-aa480e7e0b8c */)
    raw(TEMPLATE_HTML, 192, 28)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html lang="en"><head>
    <title>KTML Compiler Error</title>
</head><body>
<h1>Compiler Error</h1>
<div style="color: darkred; font-weight: bold; line-height: 20px">
    <pre></pre>
</div>
</body></html>"""
