package dev.ktml.ktor

import dev.ktml.KtmlEngine
import io.ktor.server.application.*

val KtmlPlugin = createApplicationPlugin(name = "KTML", createConfiguration = ::KtmlConfig) {
    val registry = pluginConfig.registry
    requireNotNull(registry) {
        """
            You must configure the KTML registry when you install the KtmlPlugin. Like this:
            install(KtmlPlugin) {
                registry = KtmlRegistry
            }
        """.trimIndent()
    }

    application.attributes.put(ktmlEngineKey, KtmlEngine(registry))
}
