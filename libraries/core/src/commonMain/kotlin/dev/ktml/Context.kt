package dev.ktml

import com.mohamedrejeb.ksoup.entities.KsoupEntities.encodeHtml

interface ContentWriter {
    fun write(content: String)
}

typealias Content = Context.() -> Unit

class Context(private val writer: ContentWriter, model: Map<String, Any?> = mapOf()) {
    private val _model = model.toMutableMap()

    fun write(content: Content?) = also { if (content != null) content(it) }
    fun write(content: Any?) = if (content == null) this else raw(encodeHtml(content.toString()))
    fun raw(content: String?) = also { if (!content.isNullOrEmpty()) writer.write(content) }

    inline fun <reified T> required(name: String): T {
        if (!containsKey(name)) error("Missing required context value '$name'")
        val value = get(name) ?: error("Context value '$name' is null but cannot be null")
        if (value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    inline fun <reified T> requiredNullable(name: String): T? {
        if (!containsKey(name)) error("Missing required context value '$name'")
        val value = get(name)
        if (value != null && value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    inline fun <reified T : Any> optional(name: String, defaultValue: T): T {
        val value = get(name) ?: defaultValue
        if (value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    inline fun <reified T> optionalNullable(name: String): T? {
        val value = get(name)
        if (value != null && value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    operator fun set(key: String, value: Any?) {
        _model[key] = value
    }

    operator fun get(key: String) = _model[key]

    fun containsKey(key: String) = _model.containsKey(key)

    fun remove(key: String) {
        _model.remove(key)
    }
}

class StringContentWriter : ContentWriter {
    private val buffer = StringBuilder()

    override fun write(content: String) {
        buffer.append(content)
    }

    override fun toString() = buffer.toString()
}
