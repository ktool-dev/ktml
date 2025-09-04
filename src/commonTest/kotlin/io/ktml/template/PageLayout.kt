package io.ktml.template

import io.ktml.Content
import io.ktml.HtmlWriter

fun HtmlWriter.writePageLayout(
    title: String = "No Title",
    header: Content,
    body: Content
) {
    raw("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>")
    text(title)
    raw("</title></head><body><div class=\"header\">")
    content(header)
    raw("</div><div class=\"content\">")
    content(body)
    raw("</div></body></html>")
}
