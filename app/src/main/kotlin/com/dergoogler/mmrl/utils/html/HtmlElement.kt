package com.dergoogler.mmrl.utils.html

open class HtmlElement(val tag: String) {
    private val children = mutableListOf<HtmlElement>()
    val attributes = mutableMapOf<String, String>()
    private var textContent: String? = null

    fun attr(name: String, value: String) {
        attributes[name] = value
    }

    fun text(content: String) {
        textContent = content
    }

    fun render(): String {
        val attrString = attributes.entries.joinToString(" ") { """${it.key}="${it.value}"""" }
        val innerContent = children.joinToString("") { it.render() }
        val text = textContent ?: ""

        return if (children.isEmpty() && textContent == null) {
            "<$tag $attrString/>"
        } else {
            "<$tag $attrString>$text$innerContent</$tag>"
        }
    }

    protected fun <T : HtmlElement> initTag(tag: T, block: T.() -> Unit): T {
        tag.apply(block)
        children.add(tag)
        return tag
    }

    operator fun String.unaryPlus() {
        text(this)
    }
}