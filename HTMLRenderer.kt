import java.nio.file.Files
import java.nio.file.Paths

sealed interface Element

sealed interface TextElement : Element {
    val text: String
}

sealed interface TaggedElement : Element {
    val tag: String
    val openTag: String
        get() = "<$tag>"
    val closeTag: String
        get() = "</$tag>"
}

sealed interface TaggedTextElement : TaggedElement, TextElement

sealed interface ContainerElement : TaggedElement {
    val elements: List<Element>
}

data class Text(override val text: String) : TextElement

data class Paragraph(override val text: String) : TaggedTextElement {
    override val tag: String
        get() = "p"
}

data class Heading(override val text: String, val level: Int = 1) : TaggedTextElement {
    init {
        if (level !in 1..6) {
            throw IllegalArgumentException("Invalid level $level for heading. Level must be between 1 and 6!")
        }
    }

    override val tag: String
        get() = "h$level"
}

data class Div(override val elements: List<Element>) : ContainerElement {
    constructor(vararg element: Element) : this(element.toList())

    override val tag: String
        get() = "div"
}

data class HTMLList(override val elements: List<ListItem>, val ordered: Boolean) : ContainerElement {
    constructor(ordered: Boolean, vararg element: ListItem) : this(element.toList(), ordered)

    override val tag: String
        get() = if (ordered) "ol" else "ul"
}

data class ListItem(override val elements: List<Element>) : ContainerElement {
    constructor(vararg element: Element) : this(element.toList())

    override val tag: String
        get() = "li"
}

data class Page(val title: String, val elements: List<Element>) {
    constructor(title: String, vararg element: Element) : this(title, element.toList())
}

object HTMLRenderer {
    fun render(elem: Element, indent: Int = 0): String {
        return when (elem) {
            is TaggedTextElement -> elem.openTag.indented(indent) + elem.text + elem.closeTag
            is TextElement -> elem.text.indented(indent)
            is ContainerElement -> buildString {
                this.appendLine(elem.openTag.indented(indent))
                for (el in elem.elements) {
                    appendLine(render(el, indent + 2))
                }
                this.append(elem.closeTag.indented(indent))
            }
        }
    }

    fun render(page: Page): String {
        return buildString {
            this.appendLine("<html>")
            this.appendLine("<header>".indented(2))
            this.appendLine("<title>${page.title}</title>".indented(4))
            this.appendLine("</header>".indented(2))
            this.appendLine("<body>".indented(2))
            for (elem in page.elements) {
                this.appendLine(render(elem, 4))
            }
            this.appendLine("</body>".indented(2))
            this.appendLine("</html>")
        }
    }
}

fun String.indented(indent: Int = 2, char: String = " "): String {
    return char.repeat(indent) + this
}
fun String.text(): Text = Text(this)
fun String.p(): Paragraph = Paragraph(this)
fun String.h(level: Int): Heading = Heading(this, level)
fun String.h1(): Heading = this.h(1)
fun String.h2(): Heading = this.h(2)
fun String.h3(): Heading = this.h(3)
fun String.h4(): Heading = this.h(4)
fun String.h5(): Heading = this.h(5)
fun String.h6(): Heading = this.h(6)

fun main() {
    val page = Page(
        "My Page",
        "Welcome to the Kotlin course".h1(),
        Div(
            "Kotlin is".p(),
            HTMLList(
                true,
                ListItem(
                    "General-purpose programming language".h3(),
                    HTMLList(
                        false,
                        ListItem(
                            "Backend, Mobile, Stand-Alone, Web, ...".text()
                        )
                    )
                ),
                ListItem(
                    "Modern, multi-paradigm".h3(),
                    HTMLList(
                        false,
                        ListItem(
                            "Object-oriented, functional programming (functions as first-class citizens, …), etc .".text()
                        ),
                        ListItem(
                            "Statically typed but automatically inferred types".text()
                        )
                    )
                ),
                ListItem(
                    "Emphasis on conciseness / expressiveness / practicality".h3(),
                    HTMLList(
                        false,
                        ListItem(
                            "Goodbye Java boilerplate code (getter methods, setter methods, final, etc.)".text()
                        ),
                        ListItem(
                            "Common tasks should be short and easy".text()
                        ),
                        ListItem(
                            "Mistakes should be caught as early as possible".text()
                        ),
                        ListItem(
                            "But no cryptic operators as in Scala".text()
                        )
                    )
                ),
                ListItem(
                    "100% interoperable with Java".h3(),
                    HTMLList(
                        false,
                        ListItem(
                            "You have a Java project? Make it a Java/Kotlin project in minutes with 100% interop ".text()
                        ),
                        ListItem(
                            "Kotlin-to-Java as well as Java-to-Kotlin calls".text()
                        ),
                        ListItem(
                            "For example, Kotlin reuses Java’s existing standard library (ArrayList, etc.) and extends it with extension functions (opposed to, e.g., Scala that uses its own list implementations)".text()
                        )
                    )
                ),
            )
        )
    )

    Files.writeString(Paths.get("out.html"), HTMLRenderer.render(page))
}
