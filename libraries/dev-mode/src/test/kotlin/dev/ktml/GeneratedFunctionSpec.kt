package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

enum class UserType {
    ADMIN,
    USER,
    GUEST,
}

data class Item(val name: String)

data class User(val name: String, val type: UserType)

data class SideBarItem(val name: String, val href: String)

private const val templateDir = "src/test/ktml"

class GeneratedFunctionSpec : BddSpec({
    lateinit var engine: KtmlEngine
    lateinit var processor: KtmlDynamicRegistry

    beforeEach {
        processor = KtmlDynamicRegistry(templateDir, DEFAULT_PACKAGE, watchFiles = false, outputDir = "build/generated")
        engine = KtmlEngine(processor)
    }

    suspend fun writePage(name: String, data: Map<String, Any?> = mapOf()): String {
        val writer = StringContentWriter()
        engine.writePage(Context(writer, data), name)
        return writer.toString()
    }

    "generated dashboard template" {
        Given
        val data = mapOf(
            "userName" to "John Doe",
            "message" to "Hello, World!",
            "user" to User("John Doe", UserType.USER),
            "items" to listOf(Item("Item 1"), Item("Item 2"))
        )

        When
        val result = writePage("dashboard", data)

        Then
        result.trimIndent() shouldBe """
            <!DOCTYPE html>
            <html lang="en"><head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dashboard - John Doe</title>
                </head><body>
                <div class="header">
                    
                    <h1>Dashboard</h1>
                
                </div>
                <div class="content">
                    
                    <h1>Hello, John Doe!</h1>
                    
                    <h2>You are a user!</h2>
                    
                    <h2>You are not a guest!</h2>
                    <div class="card">
                    <div class="card-header">
                        
                            <h3>Items</h3>
                        
                    </div>
                    <div class="card-body">
                        <ul>
                            <li>Item 1 - Item 0</li><li>Item 2 - Item 1</li>
                        </ul>
                    </div>
                </div>
                    
                
                            <h2>You are not an admin!</h2>
                        
            
                    <div class="sidebar">
                    
                </div><br>
                    <button onclick="alert('Hello World!')">Click me!</button>
                
                </div>
                </body></html>""".trimIndent()
    }

    "generated test-context template" {
        When
        val result = writePage("test-context")

        Then
        result shouldContain "<div>Outer Context</div>"
        result shouldContain "<div>Inner 1</div>"
        result shouldContain "<div>Inner 2</div>"
    }

    "test-fragment can be rendered" {
        Given
        val templateName = "test-fragment"

        When
        val result = writePage(templateName, mapOf("value" to "myTestValue"))

        Then
        result shouldContain "<span>myTestValue</span>"
    }

    "reloads templates when a template is changed" {
        Given
        val templateName = "regenerated-template"
        val templateFile = File("$templateDir/regenerated-template.ktml")

        When
        val beforeContent = writePage(templateName)
        templateFile.modifyContent { replace("Before", "After") }
        processor.reprocessFile(templateFile.absolutePath, false)
        val afterContent = writePage(templateName)
        templateFile.modifyContent { replace("After", "Before") }

        Then
        beforeContent shouldContain "Hello Before"
        afterContent shouldContain "Hello After"
    }
})

private fun File.modifyContent(mod: String.() -> String) {
    writeText(readText().mod())
}
