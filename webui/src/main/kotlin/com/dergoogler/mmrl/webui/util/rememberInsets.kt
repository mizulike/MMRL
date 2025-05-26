package com.dergoogler.mmrl.webui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.dergoogler.mmrl.webui.model.Insets

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