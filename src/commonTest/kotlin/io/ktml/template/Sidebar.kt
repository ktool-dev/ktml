package io.ktml.template

import io.ktml.HtmlWriter

fun HtmlWriter.writeSidebar() {
    raw(
        """
        <div class="sidebar">
            <a href="/">Home</a>
            <a href="/about">About</a>
            <a href="/contact">Contact</a>
        </div>
    """.trimIndent()
    )
}
