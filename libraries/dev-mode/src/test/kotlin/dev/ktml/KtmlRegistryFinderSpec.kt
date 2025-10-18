package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class KtmlRegistryFinderSpec : BddSpec({
    "can load dynamic registry" {
        Given
        val basePath = ""

        When
        val result = findKtmlRegistry(basePath)

        Then
        result::class shouldBe KtmlDynamicRegistry::class
    }
})
