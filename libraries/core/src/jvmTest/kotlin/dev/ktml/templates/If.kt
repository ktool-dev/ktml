package dev.ktml.templates

import dev.ktml.Content
import dev.ktml.Context

fun Context.writeIf() {
    writeIf(test = required("test"), `else` = optionalNullable("else", null as Content?), content = required("content"))
}

fun Context.writeIf(test: Boolean, `else`: Content? = null, content: Content) {
    if (test) write(content) else write(`else`)
}
