package dev.ktool.ktml.templates

import dev.ktool.ktml.Context
import dev.ktool.ktml.gen.SideBarItem

fun Context.writeSidebar() {
    val contextItems: List<SideBarItem> = required("sideBarItems")
    for (item in contextItems) {
        raw("<a href=\"")
        write(item.href)
        raw("\">")
        write(item.name)
        raw("</a>")
    }
}
