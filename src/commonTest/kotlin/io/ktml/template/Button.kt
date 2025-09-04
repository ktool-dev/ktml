package io.ktml.template

import io.ktml.HtmlWriter

fun HtmlWriter.writeButton(text: String, onClick: String) {
    raw("<button onclick=\"")
    text(onClick)
    raw("\">")
    text(text)
    raw("</button>")
}
