package com.dergoogler.webui.core

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
    private val css = """
:root {
    --safe-area-inset-top: ${top}px;
    --safe-area-inset-right: ${right}px;
    --safe-area-inset-bottom: ${bottom}px;
    --safe-area-inset-left: ${left}px;
    --window-inset-top: var(--safe-area-inset-top, 0px);
    --window-inset-bottom: var(--safe-area-inset-bottom, 0px);
    --window-inset-left: var(--safe-area-inset-left, 0px);
    --window-inset-right: var(--safe-area-inset-right, 0px);
}
    """.trimIndent()

    val cssResponse = css.style
    val cssInject = """
<!-- MMRL Inset Inject -->
<style>${css.replace(Regex("[\n\t\r\\s]"), "")}</style>
    """.trimIndent()

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