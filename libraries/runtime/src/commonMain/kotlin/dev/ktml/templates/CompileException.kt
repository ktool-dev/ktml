package dev.ktml.templates

import dev.ktml.Context

suspend fun Context.writeCompileException() {
    val exception: Throwable = required("exception")
    raw(TEMPLATE_HTML, 0, 192)
    write(/* start */ exception.message /* end: d5f1b9bf-8291-42cf-b442-0367fb47e339 */)
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
