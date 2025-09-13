package dev.ktool.ktml.templates

import dev.ktool.ktml.Context
import my.app.Item
import my.app.UserType

fun Context.writeDashboard(
    userName: String,
    message: String,
    userType: UserType,
    items: List<Item>
) {
    writePageLayout(
        "Dashboard - $userName",
        header = {
            raw("<h1>Dashboard</h1>")
        }
    ) {
        raw("<h1>Hello, ")
        write(message)
        raw("!</h1>")

        if (userType == UserType.ADMIN) {
            raw("<h2>You are an admin!</h2>")
        }

        if (userType == UserType.USER) {
            raw("<h2>You are a user!</h2>")
        }

        if (userType == UserType.GUEST) {
            raw("<h2>You are a guest!</h2>")
        }

        if (userType != UserType.GUEST) {
            raw("<h2>You are not a guest!</h2>")
        }

        writeCard(header = { raw("<h3>Items</h3>") }) {
            raw("<ul>")
            for ((index, item) in items.withIndex()) {
                raw("<li>")
                write(item.name)
                raw(" - Item ")
                write(index.toString())
                raw("</li>")
            }
            raw("</ul>")
        }

        writeSidebar()

        raw("<br><hr>")

        writeMyButton("Click me!", "alert('Hello World!')")
    }
}
