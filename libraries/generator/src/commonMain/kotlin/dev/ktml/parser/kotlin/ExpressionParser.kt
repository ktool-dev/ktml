package dev.ktml.parser.kotlin

fun String.replaceKotlinExpressions(): Pair<String, List<KotlinExpression>> {
    val expressions = mutableListOf<KotlinExpression>()
    val result = StringBuilder()
    var currentLine = 0
    var currentColumn = 0
    var index = 0

    while (index < length) {
        if (indexOf("$", index) == -1) {
            result.append(substring(index))
            break
        } else if (this[index] == '\\' && this[index + 1] == '$') {
            result.append("$")
            currentColumn += 2
            index += 2
        } else if (this[index] != '$') {
            if (this[index] == '\n') {
                currentLine++
                currentColumn = 0
            } else {
                currentColumn++
            }
            result.append(this[index])
            index++
        } else if (this[index] == '$' && this[index + 1] == '{') {
            val start = index + 2
            val end = findMatchingCloseBrace(this, start)

            if (end == -1) {
                // No matching brace, treat as regular text
                result.append(this[index])
                currentColumn++
                index++
            } else {
                val expression = KotlinExpression(
                    rawContent = substring(start, end),
                    start = Location(currentLine, currentColumn),
                    noCurly = false
                )
                expressions.add(expression)
                result.append(expression.key)

                index = end + 1
                currentLine = expression.end.line
                currentColumn = expression.end.column
            }
        } else if (substring(index, minOf(index + 5, length)).hasKotlinInterpolation()) {
            // Simple interpolation like $identifier
            val match = interpolationValueRegex.find(substring(index))
            val value = match?.groupValues?.get(1) ?: ""

            if (value.isEmpty()) {
                result.append("$")
                currentColumn++
                index++
            } else {
                val expression = KotlinExpression(
                    rawContent = value,
                    start = Location(currentLine, currentColumn),
                    noCurly = true
                )
                expressions.add(expression)
                result.append(expression.key)

                index += value.length + 1
                currentColumn = expression.end.column
            }
        } else {
            result.append("$")
            currentColumn++
            index++
        }
    }

    return Pair(result.toString(), expressions)
}

private val interpolationStartRegex = """\$(?:\{|[`_a-zA-Z][_a-zA-Z0-9]*)""".toRegex()
private val interpolationValueRegex = """^\$(`[^`]+`|[_a-zA-Z][_a-zA-Z0-9]*)""".toRegex()
fun String.hasKotlinInterpolation() = interpolationStartRegex.containsMatchIn(this)

private fun findMatchingCloseBrace(text: String, startIndex: Int): Int {
    var braceCount = 1
    var i = startIndex
    var inString = false
    var stringChar = '\u0000'
    var escaped = false

    while (i < text.length && braceCount > 0) {
        val char = text[i]

        if (escaped) {
            escaped = false
            i++
            continue
        }

        when {
            char == '\\' -> escaped = true
            !inString && (char == '"' || char == '\'') -> {
                inString = true
                stringChar = char
            }

            inString && char == stringChar -> {
                inString = false
                stringChar = '\u0000'
            }

            !inString && char == '{' -> braceCount++
            !inString && char == '}' -> braceCount--
        }

        i++
    }

    return if (braceCount == 0) i - 1 else -1
}