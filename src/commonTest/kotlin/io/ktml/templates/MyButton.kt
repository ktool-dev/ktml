package io.ktml.templates

import io.ktml.Context

fun Context.writeMyButton(text: String, onClick: String) {
    raw("<button onclick=\"")
    write(onClick)
    raw("\">")
    write(text)
    raw("</button>")
}
