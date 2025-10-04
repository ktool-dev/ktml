package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

val test: Content = {
    writePageLayout(title = optional("title", "No Title}"), header = required("header"), content = required("content"))
}

fun Context.writePageLayout() {
    writePageLayout(title = optional("title", "No Title}"), header = required("header"), content = required("content"))
}

fun Context.writePageLayout(title: String = "No Title", header: Content, content: Content) {
    raw(RAW_CONTENT_0)
    write(title)
    raw(RAW_CONTENT_1)
    write(header)
    raw(RAW_CONTENT_2)
    write(content)
    raw(RAW_CONTENT_3)
}

private const val RAW_CONTENT_0: String = """<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>"""

private const val RAW_CONTENT_1: String = """</title>
    </head>
    <body>
    <div class="header">
        """

private const val RAW_CONTENT_2: String = """
    </div>
    <div class="content">
        """

private const val RAW_CONTENT_3: String = """
    </div>
    </body>
    </html>"""
