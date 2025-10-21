package dev.ktml.util

import dev.ktool.gen.types.Import

fun String.toCamelCase(): String {
    return split("-").joinToString("") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

fun String.toPascalCase(): String {
    return toCamelCase().replaceFirstChar { it.lowercase() }
}

fun String.toKebabCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()
}

internal data class Path(val path: String) {
    override fun toString() = path
}

internal fun String.toPath() = Path(this)

fun String.isNotVoidTag() = !VOID_TAGS.any { it.equals(this, ignoreCase = true) }

fun String.toImport() = Import(
    packagePath = substringAfter("import ").substringBefore(" as "),
    alias = if (contains(" as ")) substringAfter(" as ") else null
)

const val ROOT_PACKAGE = "dev.ktml.templates"
const val ROOT_PACKAGE_PATH = "dev/ktml/templates"

const val SET_CONTEXT_VALUE_TAG = "context"
const val CONTEXT_PARAM_PREFIX = "$"

private val VOID_TAGS = listOf(
    "br",
    "hr",
    "img",
    "input",
    "meta",
    "link",
    "area",
    "base",
    "col",
    "embed",
    "param",
    "source",
    "track",
    "wbr",
    "context-get"
)

fun String.isHtmlElement() = knownHtmlElements.any { it.equals(this, ignoreCase = true) }

fun String.isSvgElement() = svgElements.any { it.equals(this, ignoreCase = true) }

private val knownHtmlElements = listOf(
    // Document metadata
    "base",
    "head",
    "link",
    "meta",
    "style",
    "title",
    // Content sectioning
    "address",
    "article",
    "aside",
    "footer",
    "header",
    "h1",
    "h2",
    "h3",
    "h4",
    "h5",
    "h6",
    "main",
    "nav",
    "section",
    "search",
    // Text content
    "blockquote",
    "dd",
    "div",
    "dl",
    "dt",
    "figcaption",
    "figure",
    "hr",
    "li",
    "menu",
    "ol",
    "p",
    "pre",
    "ul",
    // Inline text
    "a",
    "abbr",
    "b",
    "bdi",
    "bdo",
    "br",
    "cite",
    "code",
    "data",
    "dfn",
    "em",
    "i",
    "kbd",
    "mark",
    "q",
    "rp",
    "rt",
    "ruby",
    "s",
    "samp",
    "small",
    "span",
    "strong",
    "sub",
    "sup",
    "time",
    "u",
    "var",
    "wbr",
    // Image and multimedia
    "area",
    "audio",
    "img",
    "map",
    "track",
    "video",
    // Embedded content
    "embed",
    "iframe",
    "object",
    "picture",
    "portal",
    "source",
    // Scripting
    "canvas",
    "noscript",
    "script",
    // Tables
    "caption",
    "col",
    "colgroup",
    "table",
    "tbody",
    "td",
    "tfoot",
    "th",
    "thead",
    "tr",
    // Forms
    "button",
    "datalist",
    "fieldset",
    "form",
    "input",
    "label",
    "legend",
    "meter",
    "optgroup",
    "option",
    "output",
    "progress",
    "select",
    "textarea",
    // Interactive
    "details",
    "dialog",
    "summary",
    // Web components
    "slot",
    "template"
)

private val svgElements = listOf(
    "svg",
    "circle",
    "ellipse",
    "line",
    "polygon",
    "polyline",
    "rect",
    "path",
    "g",
    "defs",
    "use",
    "symbol",
    "marker",
    "mask",
    "pattern",
    "clipPath",
    "linearGradient",
    "radialGradient",
    "stop",
    "image",
    "text",
    "textPath",
    "tspan",
    "animate",
    "animateMotion",
    "animateTransform",
    "mpath",
    "set",
    "desc",
    "metadata",
    "foreignObject",
    "view",
    "switch",
    "feBlend",
    "feColorMatrix",
    "feComponentTransfer",
    "feComposite",
    "feConvolveMatrix",
    "feDiffuseLighting",
    "feDisplacementMap",
    "feDistantLight",
    "feDropShadow",
    "feFlood",
    "feFuncA",
    "feFuncB",
    "feFuncG",
    "feFuncR",
    "feGaussianBlur",
    "feImage",
    "feMerge",
    "feMergeNode",
    "feMorphology",
    "feOffset",
    "fePointLight",
    "feSpecularLighting",
    "feSpotLight",
    "feTile",
    "feTurbulence",
    "filter",
)
