package io.ktml.template

import io.ktml.Content
import io.ktml.HtmlWriter


fun HtmlWriter.writeCard(header: Content, body: Content) {
    raw("<div class=\"card\"><div class=\"card-header\">")
    content(header)
    raw("</div><div class=\"card-body\">")
    content(body)
    raw("</div></div>")
}
