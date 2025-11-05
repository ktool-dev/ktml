package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class KtmlRegistryFinderSpec : BddSpec({
    "can load dynamic registry" {
        Given
        val basePath = ""

        When
        val result = findKtmlRegistry(DEFAULT_PACKAGE, basePath)

        Then
        result::class shouldBe KtmlDynamicRegistry::class
    }
})
