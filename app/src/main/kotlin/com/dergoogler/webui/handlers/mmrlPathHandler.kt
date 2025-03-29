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
import com.dergoogler.webui.core.noResponse
import com.dergoogler.webui.core.style
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
        append(":root {\n")
        append("    /* App Base Colors */\n")
        append("    --primary: ${colorScheme.primary.toCssValue()};\n")
        append("    --onPrimary: ${colorScheme.onPrimary.toCssValue()};\n")
        append("    --primaryContainer: ${colorScheme.primaryContainer.toCssValue()};\n")
        append("    --onPrimaryContainer: ${colorScheme.onPrimaryContainer.toCssValue()};\n")
        append("    --inversePrimary: ${colorScheme.inversePrimary.toCssValue()};\n")
        append("    --secondary: ${colorScheme.secondary.toCssValue()};\n")
        append("    --onSecondary: ${colorScheme.onSecondary.toCssValue()};\n")
        append("    --secondaryContainer: ${colorScheme.secondaryContainer.toCssValue()};\n")
        append("    --onSecondaryContainer: ${colorScheme.onSecondaryContainer.toCssValue()};\n")
        append("    --tertiary: ${colorScheme.tertiary.toCssValue()};\n")
        append("    --onTertiary: ${colorScheme.onTertiary.toCssValue()};\n")
        append("    --tertiaryContainer: ${colorScheme.tertiaryContainer.toCssValue()};\n")
        append("    --onTertiaryContainer: ${colorScheme.onTertiaryContainer.toCssValue()};\n")
        append("    --background: ${colorScheme.background.toCssValue()};\n")
        append("    --onBackground: ${colorScheme.onBackground.toCssValue()};\n")
        append("    --surface: ${colorScheme.surface.toCssValue()};\n")
        append(
            "    --tonalSurface: ${
                colorScheme.surfaceColorAtElevation(1.dp).toCssValue()
            };\n"
        )
        append("    --onSurface: ${colorScheme.onSurface.toCssValue()};\n")
        append("    --surfaceVariant: ${colorScheme.surfaceVariant.toCssValue()};\n")
        append("    --onSurfaceVariant: ${colorScheme.onSurfaceVariant.toCssValue()};\n")
        append("    --surfaceTint: ${colorScheme.surfaceTint.toCssValue()};\n")
        append("    --inverseSurface: ${colorScheme.inverseSurface.toCssValue()};\n")
        append("    --inverseOnSurface: ${colorScheme.inverseOnSurface.toCssValue()};\n")
        append("    --error: ${colorScheme.error.toCssValue()};\n")
        append("    --onError: ${colorScheme.onError.toCssValue()};\n")
        append("    --errorContainer: ${colorScheme.errorContainer.toCssValue()};\n")
        append("    --onErrorContainer: ${colorScheme.onErrorContainer.toCssValue()};\n")
        append("    --outline: ${colorScheme.outline.toCssValue()};\n")
        append("    --outlineVariant: ${colorScheme.outlineVariant.toCssValue()};\n")
        append("    --scrim: ${colorScheme.scrim.toCssValue()};\n")
        append("    --surfaceBright: ${colorScheme.surfaceBright.toCssValue()};\n")
        append("    --surfaceDim: ${colorScheme.surfaceDim.toCssValue()};\n")
        append("    --surfaceContainer: ${colorScheme.surfaceContainer.toCssValue()};\n")
        append("    --surfaceContainerHigh: ${colorScheme.surfaceContainerHigh.toCssValue()};\n")
        append("    --surfaceContainerHighest: ${colorScheme.surfaceContainerHighest.toCssValue()};\n")
        append("    --surfaceContainerLow: ${colorScheme.surfaceContainerLow.toCssValue()};\n")
        append("    --surfaceContainerLowest: ${colorScheme.surfaceContainerLowest.toCssValue()};\n")
        append("    /* Filled Tonal Button Colors */\n")
        append("    --filledTonalButtonContentColor: ${filledTonalButtonColors.contentColor.toCssValue()};\n")
        append("    --filledTonalButtonContainerColor: ${filledTonalButtonColors.containerColor.toCssValue()};\n")
        append("    --filledTonalButtonDisabledContentColor: ${filledTonalButtonColors.disabledContentColor.toCssValue()};\n")
        append("    --filledTonalButtonDisabledContainerColor: ${filledTonalButtonColors.disabledContainerColor.toCssValue()};\n")
        append("    /* Filled Card Colors */\n")
        append("    --filledCardContentColor: ${cardColors.contentColor.toCssValue()};\n")
        append("    --filledCardContainerColor: ${cardColors.containerColor.toCssValue()};\n")
        append("    --filledCardDisabledContentColor: ${cardColors.disabledContentColor.toCssValue()};\n")
        append("    --filledCardDisabledContainerColor: ${cardColors.disabledContainerColor.toCssValue()};\n")
        append("}")
    }.style

    return handler@{ path ->
        try {
            if (path.matches(Regex("^assets(/.*)?$"))) {
                return@handler assetsHandler(path.removePrefix("assets/"))
            }

            if (path.matches(Regex("insets\\.css"))) {
                return@handler insets.cssResponse
            }

            if (path.matches(Regex("colors\\.css"))) {
                return@handler appColors
            }

            return@handler noResponse
        } catch (e: IOException) {
            Timber.e(e, "Error opening mmrl asset path: $path")
            return@handler noResponse
        }
    }
}