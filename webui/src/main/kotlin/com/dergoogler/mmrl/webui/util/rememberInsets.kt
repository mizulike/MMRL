package com.dergoogler.mmrl.webui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.dergoogler.mmrl.webui.model.Insets

/**
 * A Composable function that remembers the system bar insets.
 *
 * This function uses `produceState` to asynchronously load the insets and update
 * the UI when they become available. It retrieves the top, bottom, left, and right
 * insets of the system bars (like status bar and navigation bar) and converts them
 * from density-independent pixels (dp) to integers.
 *
 * If any of the insets (top, bottom, left, or right) are greater than 0, it means
 * there are system bars present, and an `Insets` object containing these values is returned.
 * Otherwise, if all insets are 0 (meaning no system bars are significantly obscuring the content),
 * it returns `null`.
 *
 * This is useful for adjusting the layout of your UI elements to avoid being overlapped
 * by system bars, ensuring that critical content remains visible.
 *
 * @return An [Insets] object containing the top, bottom, left, and right system bar insets in pixels,
 *         or `null` if no significant system bar insets are detected.
 */
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