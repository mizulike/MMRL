package com.dergoogler.mmrl.ext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.util.Stack

data class StyleState(
    val color: Color? = null,
    val bg: Color? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
)

@Composable
fun String.toStyleMarkup(): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val tagRegex = Regex("""\[(\/?)(color|bg|bold|italic|underline)(?:=([^\]]+))?]""")
    val matches = tagRegex.findAll(this).toList()

    var index = 0


    val stateStack = Stack<StyleState>()
    stateStack.push(StyleState())

    while (index < this.length) {
        val match = matches.firstOrNull { it.range.first == index }
        if (match != null) {
            val isClosing = match.groupValues[1] == "/"
            val tag = match.groupValues[2]
            val value = match.groupValues[3]

            if (isClosing) {
                if (stateStack.size > 1) {
                    stateStack.pop()
                } else {
                    // invalid unmatched closing tag — render literally
                    builder.append(match.value)
                }
            } else {
                val closingTag = "[/${tag}]"
                val closingIndex = this.indexOf(closingTag, match.range.last + 1)
                if (closingIndex != -1) {
                    val prev = stateStack.peek()
                    val newState = when (tag) {
                        "color" -> prev.copy(color = colorFromName(value))
                        "bg" -> prev.copy(bg = colorFromName(value))
                        "bold" -> prev.copy(bold = true)
                        "italic" -> prev.copy(italic = true)
                        "underline" -> prev.copy(underline = true)
                        else -> prev
                    }
                    stateStack.push(newState)

                    val innerTextStart = match.range.last + 1
                    val innerTextEnd = closingIndex

                    val innerText = this.substring(innerTextStart, innerTextEnd)
                    builder.append(
                        innerText.toStyleMarkupInner(newState)
                    )

                    stateStack.pop()
                    index = closingIndex + closingTag.length
                    continue
                } else {
                    // no closing tag — treat as literal
                    builder.append(match.value)
                }
            }
            index = match.range.last + 1
        } else {
            val nextTag = matches.firstOrNull { it.range.first > index }
            val end = nextTag?.range?.first ?: this.length
            val text = this.substring(index, end)
            val current = stateStack.peek()
            builder.append(
                AnnotatedString(
                    text,
                    spanStyle = SpanStyle(
                        color = current.color ?: Color.Unspecified,
                        background = current.bg ?: Color.Unspecified,
                        fontWeight = if (current.bold) FontWeight.Bold else null,
                        fontStyle = if (current.italic) FontStyle.Italic else null,
                        textDecoration = if (current.underline) TextDecoration.Underline else null
                    )
                )
            )
            index = end
        }
    }

    return builder.toAnnotatedString()
}

@Composable
private fun String.toStyleMarkupInner(state: StyleState): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(
        AnnotatedString(
            this,
            spanStyle = SpanStyle(
                color = state.color ?: Color.Unspecified,
                background = state.bg ?: Color.Unspecified,
                fontWeight = if (state.bold) FontWeight.Bold else null,
                fontStyle = if (state.italic) FontStyle.Italic else null,
                textDecoration = if (state.underline) TextDecoration.Underline else null
            )
        )
    )
    return builder.toAnnotatedString()
}

@Composable
fun colorFromName(name: String?): Color {
    return when (name?.lowercase()) {
        "black" -> Color.Black
        "red" -> Color.Red
        "green" -> Color.Green
        "yellow" -> Color.Yellow
        "blue" -> Color.Blue
        "magenta" -> Color.Magenta
        "cyan" -> Color.Cyan
        "white" -> Color.White
        "gray", "grey" -> Color.Gray
        "lightgray" -> Color.LightGray
        "primary" -> MaterialTheme.colorScheme.primary
        "secondary" -> MaterialTheme.colorScheme.secondary
        "tertiary" -> MaterialTheme.colorScheme.tertiary
        "background" -> MaterialTheme.colorScheme.background
        "surface" -> MaterialTheme.colorScheme.surface
        "error" -> MaterialTheme.colorScheme.error
        "outline" -> MaterialTheme.colorScheme.outline
        "inverse_surface" -> MaterialTheme.colorScheme.inverseSurface
        "inverse_on_surface" -> MaterialTheme.colorScheme.inverseOnSurface
        "inverse_primary" -> MaterialTheme.colorScheme.inversePrimary
        "surface_variant" -> MaterialTheme.colorScheme.surfaceVariant
        "on_surface_variant" -> MaterialTheme.colorScheme.onSurfaceVariant
        "surface_tint" -> MaterialTheme.colorScheme.surfaceTint
        "on_surface" -> MaterialTheme.colorScheme.onSurface
        "on_primary" -> MaterialTheme.colorScheme.onPrimary
        "on_secondary" -> MaterialTheme.colorScheme.onSecondary
        "on_tertiary" -> MaterialTheme.colorScheme.onTertiary
        "on_background" -> MaterialTheme.colorScheme.onBackground
        "on_error" -> MaterialTheme.colorScheme.onError
        else -> Color.Unspecified
    }
}
