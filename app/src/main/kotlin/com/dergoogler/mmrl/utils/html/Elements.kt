package com.dergoogler.mmrl.utils.html

// Core HTML Elements
class Html : HtmlElement("html") {
    fun head(block: Head.() -> Unit) = initTag(Head(), block)
    fun body(block: Body.() -> Unit) = initTag(Body(), block)
}

class Head : HtmlElement("head") {
    fun title(block: Title.() -> Unit) = initTag(Title(), block)
    fun meta(block: Meta.() -> Unit) = initTag(Meta(), block)
    fun link(block: Link.() -> Unit) = initTag(Link(), block)
    fun script(block: Script.() -> Unit) = initTag(Script(), block)
}

class Title : HtmlElement("title")
class Meta : HtmlElement("meta")
class Link : HtmlElement("link")
class Script : HtmlElement("script")

// Body Elements
class Body : HtmlElement("body") {
    fun div(block: Div.() -> Unit) = initTag(Div(), block)
    fun p(block: P.() -> Unit) = initTag(P(), block)
    fun span(block: Span.() -> Unit) = initTag(Span(), block)
    fun ul(block: Ul.() -> Unit) = initTag(Ul(), block)
    fun ol(block: Ol.() -> Unit) = initTag(Ol(), block)
    fun table(block: Table.() -> Unit) = initTag(Table(), block)
    fun button(block: Button.() -> Unit) = initTag(Button(), block)
    fun input(block: Input.() -> Unit) = initTag(Input(), block)
    fun form(block: Form.() -> Unit) = initTag(Form(), block)
    fun h1(block: H1.() -> Unit) = initTag(H1(), block)
    fun h2(block: H2.() -> Unit) = initTag(H2(), block)
    fun h3(block: H3.() -> Unit) = initTag(H3(), block)
    fun h4(block: H4.() -> Unit) = initTag(H4(), block)
    fun h5(block: H5.() -> Unit) = initTag(H5(), block)
    fun h6(block: H6.() -> Unit) = initTag(H6(), block)
    fun img(block: Img.() -> Unit) = initTag(Img(), block)
    fun br() = initTag(Br(), {})
    fun b(block: B.() -> Unit) = initTag(B(), block)
    fun i(block: I.() -> Unit) = initTag(I(), block)
    fun svg(block: Svg.() -> Unit) = initTag(Svg(), block)
}

// Text Elements
class P : HtmlElement("p")
class Span : HtmlElement("span")
class Div : HtmlElement("div")

// Headings
class H1 : HtmlElement("h1")
class H2 : HtmlElement("h2")
class H3 : HtmlElement("h3")
class H4 : HtmlElement("h4")
class H5 : HtmlElement("h5")
class H6 : HtmlElement("h6")

// Lists
class Ul : HtmlElement("ul") {
    fun li(block: Li.() -> Unit) = initTag(Li(), block)
}
class Ol : HtmlElement("ol") {
    fun li(block: Li.() -> Unit) = initTag(Li(), block)
}
class Li : HtmlElement("li")

// Forms
class Form : HtmlElement("form") {
    fun input(block: Input.() -> Unit) = initTag(Input(), block)
    fun button(block: Button.() -> Unit) = initTag(Button(), block)
}
class Input : HtmlElement("input")
class Button : HtmlElement("button") {
    var onClick: String
        get() = attributes["onclick"] ?: ""
        set(value) { attr("onclick", value) }
}

// Tables
class Table : HtmlElement("table") {
    fun tr(block: Tr.() -> Unit) = initTag(Tr(), block)
}
class Tr : HtmlElement("tr") {
    fun td(block: Td.() -> Unit) = initTag(Td(), block)
}
class Td : HtmlElement("td")

// Media
class Img : HtmlElement("img")
class Br : HtmlElement("br")
class B : HtmlElement("b")
class I : HtmlElement("i")
class Path : HtmlElement("path")
class G : HtmlElement("G")
class Svg : HtmlElement("svg") {
    fun path(block: Path.() -> Unit) = initTag(Path(), block)
    fun g(block: G.() -> Unit) = initTag(G(), block)
}


// DSL Entry Function
fun html(block: Html.() -> Unit): String {
    return Html().apply(block).render()
}