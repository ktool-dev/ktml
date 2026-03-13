package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

class KtmlFileWatcherSpec : BddSpec({
    "detects changes in templates nested multiple folders deep" {
        Given
        val templateRoot = createTempDirectory("ktml-watcher").toFile()
        val nestedDirectory = File(templateRoot, "one/two/three").apply { mkdirs() }
        val templateFile = File(nestedDirectory, "deep-template.ktml").apply { writeText("<div>Before</div>") }
        val changed = CountDownLatch(1)

        KtmlFileWatcher(templateRoot.absolutePath) { path, itemDeleted ->
            if (path == templateFile.absolutePath && !itemDeleted) {
                changed.countDown()
            }
        }.start()

        try {
            When
            Thread.sleep(250)
            templateFile.writeText("<div>After</div>")
            val updateDetected = changed.await(5, TimeUnit.SECONDS)

            Then
            updateDetected shouldBe true
        } finally {
            templateRoot.deleteRecursively()
        }
    }
})
