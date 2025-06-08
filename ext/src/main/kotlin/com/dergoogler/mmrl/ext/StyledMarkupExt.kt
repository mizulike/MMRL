package com.dergoogler.mmrl.ext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun String.toStyleMarkup(): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val regex = Regex("""\[(color|bg|bold|italic|underline)(=([^]]+))?]""")

    var lastIndex = 0
    var currentColor: Color? = null
    var currentBg: Color? = null
    var isBold = false
    var isItalic = false
    var isUnderline = false

    regex.findAll(this).forEach { match ->
        val start = match.range.first

        if (lastIndex < start) {
            builder.append(
                AnnotatedString(
                    this.substring(lastIndex, start),
                    spanStyle = SpanStyle(
                        color = currentColor ?: Color.Unspecified,
                        background = currentBg ?: Color.Unspecified,
                        fontWeight = if (isBold) FontWeight.Bold else null,
                        fontStyle = if (isItalic) FontStyle.Italic else null,
                        textDecoration = if (isUnderline) TextDecoration.Underline else null
                    )
                )
            )
        }

        // Parse tag
        when (match.groupValues[1]) {
            "color" -> currentColor = colorFromName(match.groupValues[3])
            "bg" -> currentBg = colorFromName(match.groupValues[3])
            "bold" -> isBold = true
            "italic" -> isItalic = true
            "underline" -> isUnderline = true
        }

        lastIndex = match.range.last + 1
    }

    // Remaining text
    if (lastIndex < this.length) {
        builder.append(
            AnnotatedString(
                this.substring(lastIndex),
                spanStyle = SpanStyle(
                    color = currentColor ?: Color.Unspecified,
                    background = currentBg ?: Color.Unspecified,
                    fontWeight = if (isBold) FontWeight.Bold else null,
                    fontStyle = if (isItalic) FontStyle.Italic else null,
                    textDecoration = if (isUnderline) TextDecoration.Underline else null
                )
            )
        )
    }

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
