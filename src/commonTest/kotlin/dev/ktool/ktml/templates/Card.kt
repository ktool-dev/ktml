package dev.ktool.ktml.templates

import dev.ktool.ktml.Content
import dev.ktool.ktml.Context

fun Context.writeCard(header: Content, body: Content) {
    raw("<div class=\"card\"><div class=\"card-header\">")
    write(header)
    raw("</div><div class=\"card-body\">")
    write(body)
    raw("</div></div>")
}
