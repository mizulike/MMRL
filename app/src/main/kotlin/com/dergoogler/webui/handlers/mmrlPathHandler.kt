package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.theme.toCssValue
import com.dergoogler.webui.core.LocalInsets
import com.dergoogler.webui.core.asStyleResponse
import com.dergoogler.webui.core.noResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun mmrlPathHandler(): (String) -> WebResourceResponse {
    val colorScheme = MaterialTheme.colorScheme
    val filledTonalButtonColors = ButtonDefaults.filledTonalButtonColors()
    val cardColors = CardDefaults.cardColors()
    val insets = LocalInsets.current

    val assetsHandler = assetsPathHandler()

    val appColors = buildString {
        appendLine(":root {")
        appendLine("\t/* App Base Colors */")
        appendLine("\t--primary: ${colorScheme.primary.toCssValue()};")
        appendLine("\t--onPrimary: ${colorScheme.onPrimary.toCssValue()};")
        appendLine("\t--primaryContainer: ${colorScheme.primaryContainer.toCssValue()};")
        appendLine("\t--onPrimaryContainer: ${colorScheme.onPrimaryContainer.toCssValue()};")
        appendLine("\t--inversePrimary: ${colorScheme.inversePrimary.toCssValue()};")
        appendLine("\t--secondary: ${colorScheme.secondary.toCssValue()};")
        appendLine("\t--onSecondary: ${colorScheme.onSecondary.toCssValue()};")
        appendLine("\t--secondaryContainer: ${colorScheme.secondaryContainer.toCssValue()};")
        appendLine("\t--onSecondaryContainer: ${colorScheme.onSecondaryContainer.toCssValue()};")
        appendLine("\t--tertiary: ${colorScheme.tertiary.toCssValue()};")
        appendLine("\t--onTertiary: ${colorScheme.onTertiary.toCssValue()};")
        appendLine("\t--tertiaryContainer: ${colorScheme.tertiaryContainer.toCssValue()};")
        appendLine("\t--onTertiaryContainer: ${colorScheme.onTertiaryContainer.toCssValue()};")
        appendLine("\t--background: ${colorScheme.background.toCssValue()};")
        appendLine("\t--onBackground: ${colorScheme.onBackground.toCssValue()};")
        appendLine("\t--surface: ${colorScheme.surface.toCssValue()};")
        appendLine("\t--tonalSurface: ${colorScheme.surfaceColorAtElevation(1.dp).toCssValue()};")
        appendLine("\t--onSurface: ${colorScheme.onSurface.toCssValue()};")
        appendLine("\t--surfaceVariant: ${colorScheme.surfaceVariant.toCssValue()};")
        appendLine("\t--onSurfaceVariant: ${colorScheme.onSurfaceVariant.toCssValue()};")
        appendLine("\t--surfaceTint: ${colorScheme.surfaceTint.toCssValue()};")
        appendLine("\t--inverseSurface: ${colorScheme.inverseSurface.toCssValue()};")
        appendLine("\t--inverseOnSurface: ${colorScheme.inverseOnSurface.toCssValue()};")
        appendLine("\t--error: ${colorScheme.error.toCssValue()};")
        appendLine("\t--onError: ${colorScheme.onError.toCssValue()};")
        appendLine("\t--errorContainer: ${colorScheme.errorContainer.toCssValue()};")
        appendLine("\t--onErrorContainer: ${colorScheme.onErrorContainer.toCssValue()};")
        appendLine("\t--outline: ${colorScheme.outline.toCssValue()};\n")
        appendLine("\t--outlineVariant: ${colorScheme.outlineVariant.toCssValue()};")
        appendLine("\t--scrim: ${colorScheme.scrim.toCssValue()};\n")
        appendLine("\t--surfaceBright: ${colorScheme.surfaceBright.toCssValue()};")
        appendLine("\t--surfaceDim: ${colorScheme.surfaceDim.toCssValue()};")
        appendLine("\t--surfaceContainer: ${colorScheme.surfaceContainer.toCssValue()};")
        appendLine("\t--surfaceContainerHigh: ${colorScheme.surfaceContainerHigh.toCssValue()};")
        appendLine("\t--surfaceContainerHighest: ${colorScheme.surfaceContainerHighest.toCssValue()};")
        appendLine("\t--surfaceContainerLow: ${colorScheme.surfaceContainerLow.toCssValue()};")
        appendLine("\t--surfaceContainerLowest: ${colorScheme.surfaceContainerLowest.toCssValue()};")
        appendLine("\t/* Filled Tonal Button Colors */")
        appendLine("\t--filledTonalButtonContentColor: ${filledTonalButtonColors.contentColor.toCssValue()};")
        appendLine("\t--filledTonalButtonContainerColor: ${filledTonalButtonColors.containerColor.toCssValue()};")
        appendLine("\t--filledTonalButtonDisabledContentColor: ${filledTonalButtonColors.disabledContentColor.toCssValue()};")
        appendLine("\t--filledTonalButtonDisabledContainerColor: ${filledTonalButtonColors.disabledContainerColor.toCssValue()};")
        appendLine("\t/* Filled Card Colors */")
        appendLine("\t--filledCardContentColor: ${cardColors.contentColor.toCssValue()};")
        appendLine("\t--filledCardContainerColor: ${cardColors.containerColor.toCssValue()};")
        appendLine("\t--filledCardDisabledContentColor: ${cardColors.disabledContentColor.toCssValue()};")
        appendLine("\t--filledCardDisabledContainerColor: ${cardColors.disabledContainerColor.toCssValue()};")
        append("}")
    }

    return handler@{ path ->
        try {
            if (path.matches(Regex("^assets(/.*)?$"))) {
                return@handler assetsHandler(path.removePrefix("assets/"))
            }

            if (path.matches(Regex("insets\\.css"))) {
                return@handler insets.cssResponse
            }

            if (path.matches(Regex("colors\\.css"))) {
                return@handler appColors.asStyleResponse()
            }

            return@handler noResponse
        } catch (e: IOException) {
            Timber.e(e, "Error opening mmrl asset path: $path")
            return@handler noResponse
        }
    }
}