package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ContextSpec : BddSpec({
    "write with Content lambda" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.write { raw("test content") }

        Then
        writer.toString() shouldBe "test content"
    }

    "write with null Content does nothing" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.write(null as Content?)

        Then
        writer.toString() shouldBe ""
    }

    "write with Any encodes HTML" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.write("<script>alert('xss')</script>")

        Then
        writer.toString() shouldBe "&lt;script&gt;alert(&apos;xss&apos;)&lt;/script&gt;"
    }

    "raw writes content without encoding" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.raw("<div>content</div>")

        Then
        writer.toString() shouldBe "<div>content</div>"
    }

    "raw with offset and length writes substring" {
        Given
        val writer = StringContentWriter()
        val context = Context(writer)

        When
        context.raw("0123456789", 2, 5)

        Then
        writer.toString() shouldBe "23456"
    }

    "required returns value when present" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to "value"))

        When
        val result = context.required<String>("key")

        Then
        result shouldBe "value"
    }

    "required throws when key missing" {
        Given
        val context = Context(StringContentWriter())

        Expect
        val exception = shouldThrow<IllegalStateException> {
            context.required<String>("missing")
        }
        exception.message shouldContain "Missing required context value 'missing'"
    }

    "required throws when value is null" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to null))

        Expect
        val exception = shouldThrow<IllegalStateException> {
            context.required<String>("key")
        }
        exception.message shouldContain "Context value 'key' is null but cannot be null"
    }

    "requiredNullable returns null when value is null" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to null))

        When
        val result = context.requiredNullable<String>("key")

        Then
        result shouldBe null
    }

    "requiredNullable throws when key missing" {
        Given
        val context = Context(StringContentWriter())

        When
        val exception = shouldThrow<IllegalStateException> {
            context.requiredNullable<String>("missing")
        }

        Then
        exception.message shouldContain "Missing required context value 'missing'"
    }

    "optional returns value when present" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to "value"))

        When
        val result = context.optional("key", "default")

        Then
        result shouldBe "value"
    }

    "optional returns default when key missing" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.optional("missing", "default")

        Then
        result shouldBe "default"
    }

    "optional throws when value is null" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to null))

        When
        val exception = shouldThrow<IllegalStateException> {
            context.optional("key", "default")
        }

        Then
        exception.message shouldContain "Context value 'key' is null but cannot be null"
    }

    "optionalNullable returns null when value is null" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to null))

        When
        val result = context.optionalNullable("key", "default")

        Then
        result shouldBe null
    }

    "optionalNullable returns default when key missing" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.optionalNullable<String>("missing", null)

        Then
        result shouldBe null
    }

    "set and get model values" {
        Given
        val context = Context(StringContentWriter())

        When
        context["key"] = "value"

        Then
        context["key"] shouldBe "value"
    }

    "containsKey returns true when key exists" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to "value"))

        When
        val result = context.containsKey("key")

        Then
        result shouldBe true
    }

    "containsKey returns false when key missing" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.containsKey("missing")

        Then
        result shouldBe false
    }

    "remove deletes key from model" {
        Given
        val context = Context(StringContentWriter(), mapOf("key" to "value"))

        When
        context.remove("key")

        Then
        context.containsKey("key") shouldBe false
    }

    "copy merges parameters" {
        Given
        val context = Context(StringContentWriter(), mapOf("key1" to "value1"))

        When
        val copied = context.copy(mapOf("key2" to "value2"))

        Then
        copied["key1"] shouldBe "value1"
        copied["key2"] shouldBe "value2"
    }

    "copy with clear replaces model" {
        Given
        val context = Context(StringContentWriter(), mapOf("key1" to "value1"))

        When
        val copied = context.copy(mapOf("key2" to "value2"), clear = true)

        Then
        copied.containsKey("key1") shouldBe false
        copied["key2"] shouldBe "value2"
    }

    "pathParam returns value when present" {
        Given
        val context = Context(StringContentWriter(), pathParams = mapOf("id" to "123"))

        When
        val result = context.pathParam("id")

        Then
        result shouldBe "123"
    }

    "queryParam returns first value when present" {
        Given
        val context = Context(StringContentWriter(), queryParams = mapOf("filter" to listOf("first", "second")))

        When
        val result = context.queryParam("filter")

        Then
        result shouldBe "first"
    }

    "If returns value when check is true" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.If(true, "yes", "no")

        Then
        result shouldBe "yes"
    }

    "If returns elseValue when check is false" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.If(false, "yes", "no")

        Then
        result shouldBe "no"
    }

    "cssClass filters nulls and empty IfData" {
        Given
        val context = Context(StringContentWriter())

        When
        val result = context.cssClass(
            "class1",
            null,
            "class2",
            context.If(false, "no"),
            context.If(true, "yes")
        )

        Then
        result shouldBe "class1 class2 yes"
    }

    "StringContentWriter accumulates content" {
        Given
        val writer = StringContentWriter()

        When
        writer.write("hello ")
        writer.write("world")

        Then
        writer.toString() shouldBe "hello world"
    }

    "StringContentWriter clear resets buffer" {
        Given
        val writer = StringContentWriter()

        When
        writer.write("content")
        writer.clear()

        Then
        writer.toString() shouldBe ""
    }
})
