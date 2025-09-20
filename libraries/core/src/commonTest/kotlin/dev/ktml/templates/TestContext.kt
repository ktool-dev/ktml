package dev.ktml.templates

import dev.ktml.Context

fun Context.writeTestContext() {
    writeContext(
        values = mapOf("value" to "Outer Context"),
    ) {
        writeWriteContextValue()
        writeContext(
            values = mapOf("value" to "Inner 1"),
        ) {
            writeWriteContextValue()
        }
        writeContext(
            values = mapOf("value" to "Inner 2"),
        ) {
            writeWriteContextValue()
        }
    }
}
