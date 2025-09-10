package io.ktml.templates

import io.ktml.Context
import io.ktml.gen.SideBarItem

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
