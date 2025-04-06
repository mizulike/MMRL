package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.theme.toCssValue
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.core.LocalInsets
import com.dergoogler.webui.core.asScriptResponse
import com.dergoogler.webui.core.asStyleResponse
import com.dergoogler.webui.core.notFoundResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun mmrlPathHandler(viewModel: WebUIViewModel? = null): (String) -> WebResourceResponse {
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

            if (viewModel != null && path.matches(Regex("scripts/sufile-fetch-ext\\.js"))) {
                val file = viewModel.sanitizedModIdWithFile
                val inputStream = viewModel.sanitizedModIdWithFileInputStream

                return@handler """window.$file = window.$file || {};

const defaultFetchStreamOptions = {
  chunkSize: 1024 * 1024,
  signal: null,
};

window.$file.fetch = function (path, options = {}) {
  // Validate required dependencies
  if (typeof $inputStream === "undefined") {
    return Promise.reject(new Error("$inputStream is not available"));
  }

  const mergedOptions = { ...defaultFetchStreamOptions, ...options };

  return new Promise((resolve, reject) => {
    let input;
    try {
      input = $inputStream.open(path);
      if (!input) {
        throw new Error("Failed to open file input stream");
      }
    } catch (error) {
      reject(
        new Error("Failed to open file at path '" + path + "': error.message")
      );
      return;
    }

    const abortHandler = () => {
      try {
        input?.close();
      } catch (error) {
        console.error("Error during abort cleanup:", error);
      }
      reject(new DOMException("The operation was aborted.", "AbortError"));
    };

    if (mergedOptions.signal) {
      if (mergedOptions.signal.aborted) {
        abortHandler();
        return;
      }
      mergedOptions.signal.addEventListener("abort", abortHandler);
    }

    const stream = new ReadableStream({
      async pull(controller) {
        try {
          const chunkData = input.readChunk(mergedOptions.chunkSize);
          if (!chunkData) {
            controller.close();
            cleanup();
            return;
          }

          const chunk = JSON.parse(chunkData);
          if (chunk && chunk.length > 0) {
            controller.enqueue(new Uint8Array(chunk));
          } else {
            controller.close();
            cleanup();
          }
        } catch (error) {
          cleanup();
          controller.error(error);
          reject(new Error("Error reading file chunk: " + error.message));
        }
      },
      cancel() {
        cleanup();
      },
    });

    function cleanup() {
      try {
        if (mergedOptions.signal) {
          mergedOptions.signal.removeEventListener("abort", abortHandler);
        }
        input?.close();
      } catch (error) {
        console.error("Error during cleanup:", error);
      }
    }

    resolve(
      new Response(stream, {
        headers: { "Content-Type": "application/octet-stream" },
      })
    );
  });
};
""".trimIndent().asScriptResponse()
            }

            if (path.matches(Regex("insets\\.css"))) {
                return@handler insets.cssResponse
            }

            if (path.matches(Regex("colors\\.css"))) {
                return@handler appColors.asStyleResponse()
            }

            return@handler notFoundResponse
        } catch (e: IOException) {
            Timber.e(e, "Error opening mmrl asset path: $path")
            return@handler notFoundResponse
        }
    }
}