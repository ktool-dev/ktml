package io.ktml.template

import io.ktml.HtmlWriter
import my.app.Item
import my.app.UserType

fun HtmlWriter.writeDashboard(
    userName: String,
    message: String,
    userType: UserType,
    items: List<Item>
) {
    writePageLayout(
        "Dashboard - $userName",
        header = { raw("<h1>Dashboard</h1>") }) {
        raw("<h1>Hello, ").text(message).raw("!</h1>")

        if (userType == UserType.ADMIN) {
            raw("<h2>You are an admin!</h2>")
        }

        if (userType == UserType.USER) {
            raw("<h2>You are a user!</h2>")
        }

        if (userType == UserType.GUEST) {
            raw("<h2>You are a guest!</h2>")
        }

        if (!(userType == UserType.GUEST)) {
            raw("<h2>You are not a guest!</h2>")
        }

        writeCard(header = { raw("<h3>Items</h3>") }) {
            raw("<ul>")
            items.forEachIndexed { index, item ->
                raw("<li>").text(item.name).raw(" - Item ").text(index.toString()).raw("</li>")
            }
            raw("</ul>")
        }

        writeSidebar()

        raw("<br><hr>")

        writeButton("Click me!", "alert('Hello World!')")
    }
}
