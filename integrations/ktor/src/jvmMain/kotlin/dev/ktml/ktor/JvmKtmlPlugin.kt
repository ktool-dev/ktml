package dev.ktml.ktor

import dev.ktml.KtmlEngine
import dev.ktml.findKtmlRegistry
import io.ktor.server.application.*

val KtmlPlugin = createApplicationPlugin(name = "KTML") {
    application.attributes.put(ktmlEngineKey, KtmlEngine(findKtmlRegistry()))
}
