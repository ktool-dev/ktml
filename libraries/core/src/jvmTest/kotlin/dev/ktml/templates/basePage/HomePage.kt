package dev.ktml.templates.basePage

import dev.ktml.Context

fun Context.writeHomePage() {
    raw(RAW_CONTENT_0)
}

private const val RAW_CONTENT_0: String = """<h1>Home Page</h1>"""
