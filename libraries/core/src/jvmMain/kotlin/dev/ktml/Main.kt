package dev.ktml

fun main(args: Array<String>) {
    val dir = if (args.isEmpty()) System.getProperty("user.dir") + "/src/jvmTest/resources/templates" else args[0]
    JvmKtmlProcessor(
        dir,
        "build/generated/ktml",
        "build/generated/ktml-compiled",
    ).createWebApp().start()
}
