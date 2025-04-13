package com.dergoogler.mmrl.webui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection

data class Insets(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int,
) {
    private val css
        get() = buildString {
            appendLine(":root {")
            appendLine("\t--safe-area-inset-top: ${top}px;")
            appendLine("\t--safe-area-inset-right: ${right}px;")
            appendLine("\t--safe-area-inset-bottom: ${bottom}px;")
            appendLine("\t--safe-area-inset-left: ${left}px;")
            appendLine("\t--window-inset-top: var(--safe-area-inset-top, 0px);")
            appendLine("\t--window-inset-bottom: var(--safe-area-inset-bottom, 0px);")
            appendLine("\t--window-inset-left: var(--safe-area-inset-left, 0px);")
            appendLine("\t--window-inset-right: var(--safe-area-inset-right, 0px);")
            appendLine("\t--f7-safe-area-top: var(--window-inset-top, 0px) !important;")
            appendLine("\t--f7-safe-area-bottom: var(--window-inset-bottom, 0px) !important;")
            appendLine("\t--f7-safe-area-left: var(--window-inset-left, 0px) !important;")
            appendLine("\t--f7-safe-area-right: var(--window-inset-right, 0px) !important;")
            append("}")
        }

    val cssInject
        get() = buildString {
            val sdg = css
                .replace(Regex("\t"), "\t\t")
                .replace(Regex("\n\\}"), "\n\t}")

            appendLine("<!-- MMRL Insets Inject -->")
            appendLine("<style data-mmrl type=\"text/css\">")
            appendLine("\t$sdg")
            appendLine("</style>")
        }

    val cssResponse get() = css.asStyleResponse()

    companion object {
        val None = Insets(0, 0, 0, 0)
    }
}

@Composable
fun rememberInsets(): Insets? {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val insets = WindowInsets.systemBars

    val loadedInsets by produceState<Insets?>(null) {
        val top = (insets.getTop(density) / density.density).toInt()
        val bottom = (insets.getBottom(density) / density.density).toInt()
        val left = (insets.getLeft(density, layoutDirection) / density.density).toInt()
        val right = (insets.getRight(density, layoutDirection) / density.density).toInt()

        if (top > 0 || bottom > 0 || left > 0 || right > 0) {
            value = Insets(
                top = top,
                bottom = bottom,
                left = left,
                right = right
            )
        }
    }

    return loadedInsets
}

val LocalInsets = staticCompositionLocalOf<Insets> {
    error("CompositionLocal Insets not present")
}