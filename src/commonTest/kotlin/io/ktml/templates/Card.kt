package io.ktml.templates

import io.ktml.Content
import io.ktml.Context

fun Context.writeCard(header: Content, body: Content) {
    raw("<div class=\"card\"><div class=\"card-header\">")
    write(header)
    raw("</div><div class=\"card-body\">")
    write(body)
    raw("</div></div>")
}
