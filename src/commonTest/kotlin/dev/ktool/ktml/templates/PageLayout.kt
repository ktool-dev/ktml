package dev.ktool.ktml.templates

import dev.ktool.ktml.Content
import dev.ktool.ktml.Context

fun Context.writePageLayout(
    title: String = "No Title",
    header: Content,
    body: Content
) {
    raw("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>")
    write(title)
    raw("</title></head><body><div class=\"header\">")
    write(header)
    raw("</div><div class=\"content\">")
    write(body)
    raw("</div></body></html>")
}
