package dev.ktml

import dev.ktml.templates.writeCard
import dev.ktml.templates.writeDashboard
import dev.ktml.test.Item
import dev.ktml.test.User
import dev.ktml.test.UserType
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class KtmlEngineGenSpec : BddSpec({
    "generated card template" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.writeCard({ write("Hello Header") }) {
            write("Hello Body")
        }

        Then
        "    $writer".trimIndent() shouldBe """
            <div class="card">
                <div class="card-header">
                    Hello Header
                </div>
                <div class="card-body">
                    Hello Body
                </div>
            </div>
        """.trimIndent()
    }

    "generated dashboard template" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.writeDashboard(
            userName = "John Doe",
            message = "Hello, World!",
            user = User("John Doe", UserType.USER),
            items = listOf(Item("Item 1"), Item("Item 2")),
        )

        Then
        "    $writer".trimIndent() shouldBe """
            <html lang="en">
            <head>
                <meta charset="UTF-8">>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">>
                <title>Dashboard - John Doe</title>
            </head>
            <body>
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
                    
                        <header>
                            <h3>Items</h3>
                        </header>
                        <body>
                        <ul>
                            <li>Item 1 - Item 0</li><li>Item 2 - Item 1</li>
                        </ul>
                        </body>
                    
                </div>
            </div>
                    <div class="sidebar">
                
            </div>
                    <button onclick="alert&lpar;&apos;Hello World&excl;&apos;&rpar;">Click me&excl;</button>
                
            </div>
            </body>
            </html>""".trimIndent()
    }
})
