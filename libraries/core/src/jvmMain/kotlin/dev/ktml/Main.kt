package dev.ktml

fun main(args: Array<String>) {
    val dir = if (args.isEmpty()) System.getProperty("user.dir") else args[0]
    JvmKtmlProcessor(dir, "$dir/.ktml/generated", "$dir/.ktml/compiled").createWebApp().start()
}
