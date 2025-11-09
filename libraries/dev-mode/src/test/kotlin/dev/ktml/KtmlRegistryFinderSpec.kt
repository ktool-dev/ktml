package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class KtmlRegistryFinderSpec : BddSpec({
    "can load dynamic registry" {
        When
        val result = findKtmlRegistry(DEFAULT_PACKAGE)

        Then
        result::class shouldBe KtmlDynamicRegistry::class
    }
})
