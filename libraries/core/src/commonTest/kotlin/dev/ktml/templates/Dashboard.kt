package dev.ktml.templates

import dev.ktml.Context
import dev.ktml.templates.elements.writeMyButton
import dev.ktml.test.*

fun Context.writeDashboard(
    items: List<Item>,
    message: String,
    user: User,
    userName: String,
) {
    writePageLayout(
        title = "Dashboard - ${user.name}",
        header = {
            raw(RAW_CONTENT_0)
        },
    ) {
        raw(RAW_CONTENT_1)
        write(user.name)
        raw(RAW_CONTENT_2)
        if (user.type == UserType.ADMIN) {
            raw(RAW_CONTENT_3)
        }
        raw(RAW_CONTENT_4)
        if (user.type == UserType.USER) {
            raw(RAW_CONTENT_5)
        }
        raw(RAW_CONTENT_6)
        if (user.type == UserType.GUEST) {
            raw(RAW_CONTENT_7)
        }
        raw(RAW_CONTENT_8)
        if (user.type != UserType.GUEST) {
            raw(RAW_CONTENT_9)
        }
        raw(RAW_CONTENT_10)
        writeCard(
            body = {
                raw(RAW_CONTENT_11)
                for ((index, item) in items.withIndex()) {
                    raw(RAW_CONTENT_12)
                    write(item.name)
                    raw(RAW_CONTENT_13)
                    write(index)
                    raw(RAW_CONTENT_14)
                }
                raw(RAW_CONTENT_15)
            },
        ) {
            raw(RAW_CONTENT_16)
        }
        raw(RAW_CONTENT_17)
        writeIf(
            test = user.type == UserType.ADMIN,
            `else` = {
                raw(RAW_CONTENT_18)
            },
        ) {
            raw(RAW_CONTENT_19)
        }
        raw(RAW_CONTENT_20)
        writeSidebar()
        raw(RAW_CONTENT_21)
        writeMyButton(
            onClick = "alert('Hello World!')",
            text = "Click me!",
        )
        raw(RAW_CONTENT_22)
    }
}

private const val RAW_CONTENT_0 = """
            <h1>Dashboard</h1>
        """
private const val RAW_CONTENT_1 = """
            <h1>Hello, """
private const val RAW_CONTENT_2 = """!</h1>
            """
private const val RAW_CONTENT_3 = """<h2>You are an admin!</h2>"""
private const val RAW_CONTENT_4 = """
            """
private const val RAW_CONTENT_5 = """<h2>You are a user!</h2>"""
private const val RAW_CONTENT_6 = """
            """
private const val RAW_CONTENT_7 = """<h2>You are a guest!</h2>"""
private const val RAW_CONTENT_8 = """
            """
private const val RAW_CONTENT_9 = """<h2>You are not a guest!</h2>"""
private const val RAW_CONTENT_10 = """
            """
private const val RAW_CONTENT_11 = """
                <ul>
                    """
private const val RAW_CONTENT_12 = """<li>"""
private const val RAW_CONTENT_13 = """ - Item """
private const val RAW_CONTENT_14 = """</li>"""
private const val RAW_CONTENT_15 = """
                </ul>
                """
private const val RAW_CONTENT_16 = """
                    <h3>Items</h3>
                """
private const val RAW_CONTENT_17 = """
            """
private const val RAW_CONTENT_18 = """
                    <h2>You are not an admin!</h2>
                """
private const val RAW_CONTENT_19 = """<h2>You are an admin!</h2>"""
private const val RAW_CONTENT_20 = """
            """
private const val RAW_CONTENT_21 = """
            """
private const val RAW_CONTENT_22 = """
        """
