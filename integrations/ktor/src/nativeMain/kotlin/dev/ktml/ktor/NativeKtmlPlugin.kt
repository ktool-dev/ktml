package dev.ktml.ktor

import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import io.ktor.server.application.*

class KtmlConfig {
    var registry: KtmlRegistry? = null
}

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
