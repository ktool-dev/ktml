package dev.ktml.compile

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class KotlinCompileSpec : BddSpec({
    lateinit var tempDir: Path
    lateinit var sourceDir: Path
    lateinit var outputDir: Path

    beforeEach {
        tempDir = Files.createTempDirectory("kotlin")
        sourceDir = tempDir.resolve("src").createDirectories()
        outputDir = tempDir.resolve("output")
    }

    afterEach {
        tempDir.toFile().deleteRecursively()
    }

    fun String.writeSrc(fileName: String) {
        sourceDir.resolve("$fileName.kt").writeText(this)
    }

    "compile simple valid Kotlin source to directory" {
        Given
        """
            package test
            
            class SimpleClass {
                fun hello(): String = "Hello, World!"
            }
        """.writeSrc("SimpleClass")

        When
        val errors = KotlinCompile.compileFilesToDir(
            rootDir = sourceDir,
            outputDir = outputDir
        )

        Then
        errors.isEmpty() shouldBe true
        outputDir.exists() shouldBe true
    }

    "compile returns compiler errors" {
        Given
        """
            package test
            
            class InvalidClass {
                fun hello = "Hello, World!"
            }
        """.writeSrc("InvalidClass")

        When
        val errors = KotlinCompile.compileFilesToDir(
            rootDir = sourceDir,
            outputDir = outputDir
        )

        Then
        errors.size shouldBe 1
        errors[0].filePath shouldBe "InvalidClass.kt"
        errors[0].message shouldContain "Expecting '('"
    }
})