package dev.ktml.templates

import dev.ktml.Context
import dev.ktml.templates.elements.writeMyButton
import dev.ktml.test.*

fun Context.writeDashboard() {
    val items: List<Item> = required("items")
    val message: String = required("message")
    val user: User = required("user")
    val userName: String = required("userName")
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
            header = {
                raw(RAW_CONTENT_11)
            },
        ) {
            raw(RAW_CONTENT_12)
            for ((index, item) in items.withIndex()) {
                raw(RAW_CONTENT_13)
                write(item.name)
                raw(RAW_CONTENT_14)
                write(index)
                raw(RAW_CONTENT_15)
            }
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

private const val RAW_CONTENT_0: String = """
            <h1>Dashboard</h1>
        """

private const val RAW_CONTENT_1: String = """
            <h1>Hello, """

private const val RAW_CONTENT_2: String = """!</h1>
            """

private const val RAW_CONTENT_3: String = """<h2>You are an admin!</h2>"""

private const val RAW_CONTENT_4: String = """
            """

private const val RAW_CONTENT_5: String = """<h2>You are a user!</h2>"""

private const val RAW_CONTENT_6: String = """
            """

private const val RAW_CONTENT_7: String = """<h2>You are a guest!</h2>"""

private const val RAW_CONTENT_8: String = """
            """

private const val RAW_CONTENT_9: String = """<h2>You are not a guest!</h2>"""

private const val RAW_CONTENT_10: String = """
            """

private const val RAW_CONTENT_11: String = """
                    <h3>Items</h3>
                """

private const val RAW_CONTENT_12: String = """<ul>
                    """

private const val RAW_CONTENT_13: String = """<li>"""

private const val RAW_CONTENT_14: String = """ - Item """

private const val RAW_CONTENT_15: String = """</li>"""

private const val RAW_CONTENT_16: String = """
                </ul>"""

private const val RAW_CONTENT_17: String = """
            """

private const val RAW_CONTENT_18: String = """
                    <h2>You are not an admin!</h2>
                """

private const val RAW_CONTENT_19: String = """<h2>You are an admin!</h2>"""

private const val RAW_CONTENT_20: String = """
            """

private const val RAW_CONTENT_21: String = """
            """

private const val RAW_CONTENT_22: String = """
        """
