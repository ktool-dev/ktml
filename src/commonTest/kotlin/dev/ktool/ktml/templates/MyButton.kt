package dev.ktool.ktml.templates

import dev.ktool.ktml.Context

fun Context.writeMyButton(text: String, onClick: String) {
    raw("<button onclick=\"")
    write(onClick)
    raw("\">")
    write(text)
    raw("</button>")
}
