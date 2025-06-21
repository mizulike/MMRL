package com.dergoogler.mmrl.ext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import java.util.Stack
import androidx.core.graphics.toColorInt


// A data class to hold the styling state for the parser stack
private data class StyleState(
    val tag: String? = null,
    val color: Color = Color.Unspecified,
    val bg: Color = Color.Unspecified,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val link: String? = null,
)

/**
 * Parses a string with custom markup tags into an [AnnotatedString].
 *
 * This function supports nested styles and clickable links.
 * Supported tags:
 * - `[bold]...[/bold]`
 * - `[italic]...[/italic]`
 * - `[underline]...[/underline]`
 * - `[color=...]...[/color]` (e.g., #FF0000 or red)
 * - `[bg=...]...[/bg]` (e.g., #FFFF00 or yellow)
 * - `[link=...]...[/link]` (e.g., https://www.google.com)
 *
 * Mismatched or invalid tags are ignored.
 * Unclosed tags will apply their style to the end of the string.
 */
@Composable
fun String.toStyleMarkup(): AnnotatedString {
    // The regex now also validates the tag names for better parsing.
    val tagRegex = Regex("""\[(/?)(color|link|bg|bold|b|italic|i|underline|u)(?:=([^]]+))?]""")
    val matches = tagRegex.findAll(this)

    // The styleStack keeps track of nested styles.
    val styleStack = Stack<StyleState>().apply { push(StyleState()) }

    return buildAnnotatedString {
        var lastIndex = 0

        matches.forEach { match ->
            // 1. Append the plain text before this tag
            val text = this@toStyleMarkup.substring(lastIndex, match.range.first)
            if (text.isNotEmpty()) {
                applyStyle(this, text, styleStack.peek())
            }

            // 2. Process the tag
            val isClosing = match.groupValues[1] == "/"
            val tag = match.groupValues[2]

            if (isClosing) {
                // On a closing tag, pop the style if it matches the one on the stack
                if (styleStack.size > 1 && styleStack.peek().tag == tag) {
                    styleStack.pop()
                }
            } else {
                // On an opening tag, create and push a new style
                val value = match.groupValues[3]
                val current = styleStack.peek()
                val newStyle = when (tag) {
                    "color" -> current.copy(tag = tag, color = colorFromName(value))
                    "bg" -> current.copy(tag = tag, bg = colorFromName(value))

                    "b",
                    "bold",
                        -> current.copy(tag = tag, bold = true)

                    "i",
                    "italic",
                        -> current.copy(tag = tag, italic = true)

                    "u",
                    "underline",
                        -> current.copy(tag = tag, underline = true)

                    "link" -> current.copy(
                        tag = tag,
                        link = value,
                        color = MaterialTheme.colorScheme.primary, // Apply link style
                        underline = true
                    )

                    else -> current
                }
                styleStack.push(newStyle)
            }

            // 3. Move index past the processed tag
            lastIndex = match.range.last + 1
        }

        // 4. Append any remaining text after the last tag
        if (lastIndex < this@toStyleMarkup.length) {
            val remainingText = this@toStyleMarkup.substring(lastIndex)
            applyStyle(this, remainingText, styleStack.peek())
        }
    }
}

// Helper function to apply the current style and link annotation
private fun applyStyle(builder: AnnotatedString.Builder, text: String, style: StyleState) {
    val spanStyle = SpanStyle(
        color = style.color,
        background = style.bg,
        fontWeight = if (style.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (style.italic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = if (style.underline) TextDecoration.Underline else null,
    )

    if (style.link != null) {
        // For links, push a "URL" annotation with the URL as the item.
        builder.pushStringAnnotation(tag = "URL", annotation = style.link)
        builder.withStyle(style = spanStyle) {
            append(text)
        }
        builder.pop()
    } else {
        builder.withStyle(style = spanStyle) {
            append(text)
        }
    }
}

/**
 * Converts a color name or a hexadecimal string into a Compose [Color].
 *
 * @param name The string representing the color. This can be a predefined name
 * (e.g., "red", "primary") or a hex code (e.g., "#FF5733", "#CCFFFFFF").
 * @return The corresponding [Color] object, or [Color.Unspecified] if the
 * name is null or the format is invalid.
 */
@Composable
fun colorFromName(name: String?): Color {
    if (name == null) {
        return Color.Unspecified
    }

    // Add support for hex color codes (e.g., #FF0000 or #99FF0000 for ARGB)
    if (name.startsWith("#")) {
        return try {
            // Use Android's parsing utility and convert to a Compose Color
            Color(name.toColorInt())
        } catch (e: IllegalArgumentException) {
            // Return Unspecified if the hex code is invalid
            Color.Unspecified
        }
    }

    // Fall back to the existing named color mapping
    return when (name.lowercase()) {
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
        // MaterialTheme colors
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