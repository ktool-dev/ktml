package dev.ktml

interface ContentWriter {
    suspend fun write(content: String) = write(content, 0, content.length)
    suspend fun write(content: String, offset: Int, length: Int)
}

typealias Content = suspend Context.() -> Unit

class Context(
    private val writer: ContentWriter,
    model: Map<String, Any?> = mapOf(),
    val queryParams: Map<String, List<String>> = mapOf(),
    val pathParams: Map<String, String> = mapOf()
) {
    private val _model = model.toMutableMap()

    suspend fun write(content: Content?) = also { content?.invoke(it) }
    suspend fun write(context: Context) = also { writer.write("") }
    suspend fun write(content: Any?) = raw(content?.toString()?.encodeHtml())
    suspend fun raw(content: Any?) = apply { content?.toString()?.also { writer.write(it) } }
    suspend fun raw(content: String, offset: Int, length: Int) = apply { writer.write(content, offset, length) }

    inline fun <reified T> required(name: String): T =
        requiredNullable(name) ?: error("Context value '$name' is null but cannot be null")

    inline fun <reified T> requiredNullable(name: String): T? {
        if (!containsKey(name)) error("Missing required context value '$name'")
        val value = get(name)
        if (value != null && value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    inline fun <reified T : Any> optional(name: String, defaultValue: T): T {
        val value = if (containsKey(name)) get(name) else defaultValue
        if (value == null) error("Context value '$name' is null but cannot be null")
        if (value !is T) error("Context value '$name' is of type ${value::class} but type ${T::class} was expected")
        return value
    }

    inline fun <reified T : Any> optionalNullable(name: String, defaultValue: T?): T? {
        val value = if (containsKey(name)) get(name) else defaultValue
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

    fun copy(params: Map<String, Any?> = emptyMap(), clear: Boolean = false) =
        Context(writer, if (clear) params else _model + params)

    fun model(): Map<String, Any?> = _model

    fun pathParam(key: String) = pathParams[key]
    fun queryParam(key: String) = queryParams[key]?.firstOrNull()

    fun If(check: Boolean, value: Any?, elseValue: Any? = null): String? =
        if (check) value?.toString()?.takeIf { it.isNotEmpty() } else elseValue?.toString()?.takeIf { it.isNotEmpty() }

    fun cssClass(vararg values: String?) = values.filterNotNull().joinToString(separator = " ")
}

class StringContentWriter : ContentWriter {
    private val buffer = StringBuilder()

    override suspend fun write(content: String, offset: Int, length: Int) {
        buffer.append(content, offset, offset + length)
    }

    override fun toString() = buffer.toString()

    fun clear() = buffer.clear()
}

fun content(block: Content) = block
