package dev.ktml.templates

import dev.ktml.Context
import dev.ktml.test.SideBarItem

fun Context.writeSidebar() {
    val sideBarItems: List<SideBarItem> = optional("sideBarItems", listOf())
    raw(RAW_CONTENT_0)
    for (item in sideBarItems) {
        raw(RAW_CONTENT_1)
        write(item.href)
        raw(RAW_CONTENT_2)
        write(item.name)
        raw(RAW_CONTENT_3)
    }
    raw(RAW_CONTENT_4)
}

private const val RAW_CONTENT_0: String = """<div class="sidebar">
        """

private const val RAW_CONTENT_1: String = """<a href=""""

private const val RAW_CONTENT_2: String = """">"""

private const val RAW_CONTENT_3: String = """</a>"""

private const val RAW_CONTENT_4: String = """
    </div><br>"""
