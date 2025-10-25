package dev.ktml.parser.kotlin

import dev.ktool.gen.LINE_SEPARATOR
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val EXPRESSION_REPLACE_FORMAT = "__INTERPOLATION_X__"
val EXPRESSION_REPLACE_REGEX =
    "__INTERPOLATION_[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}__".toRegex()

data class Location(val line: Int, val column: Int)

@OptIn(ExperimentalUuidApi::class)
class KotlinExpression(rawContent: String, val start: Location, noCurly: Boolean) {
    val uuid = Uuid.random().toString()
    val key = EXPRESSION_REPLACE_FORMAT.replace("X", uuid)
    val end = determineEnd(start, rawContent, noCurly)
    val content = rawContent.trimIndent().trim()
    val isMultiLine = content.contains(LINE_SEPARATOR)
    val idComment = " /*id:$uuid*/"
    val kotlinFileContent = "$content$idComment"

    override fun toString() = "[uuid=$uuid start=$start, end=$end, content=$content]"
}

private fun determineEnd(start: Location, content: String, noCurly: Boolean) = content.split(LINE_SEPARATOR).let {
    if (it.size == 1) {
        Location(start.line, start.column + content.length + if (noCurly) 1 else 3)
    } else {
        Location(start.line + it.size - 1, it.last().length)
    }
}

fun List<KotlinExpression>.findByKey(key: String) = this.find { it.key == key } ?: error("Unknown expression: $key")

val ID_COMMENT_REGEX = """\s/\*id:\s*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\*/""".toRegex()

fun String.removeContentComments() = replace(ID_COMMENT_REGEX, "")
