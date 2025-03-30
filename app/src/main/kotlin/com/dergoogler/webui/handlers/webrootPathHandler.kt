package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.mmrl.utils.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.core.InjectionType
import com.dergoogler.webui.core.LocalInsets
import com.dergoogler.webui.core.addInjection
import com.dergoogler.webui.core.asResponse
import com.dergoogler.webui.core.notFoundResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun webrootPathHandler(
    viewModel: WebUIViewModel,
): (String) -> WebResourceResponse {
    val prefs = LocalUserPreferences.current
    val insets = LocalInsets.current

    val configBase = SuFile("/data/adb/.config/${viewModel.modId}")
    val customCssFile = SuFile(configBase, "custom.css")
    val customJsFile = SuFile(configBase, "custom.js")

    val directory by remember {
        mutableStateOf(
            SuFile(viewModel.webRoot).getCanonicalDirPath().toSuFile()
        )
    }

    return handler@{ path ->
        if (path.startsWith("mmrl/")) return@handler notFoundResponse
        if (path.startsWith(".adb/")) return@handler notFoundResponse
        if (path.startsWith(".${viewModel.modId}/")) return@handler notFoundResponse
        if (path.endsWith("favicon.ico") || path.startsWith("favicon.ico")) return@handler notFoundResponse

        try {
            val file = directory.getCanonicalFileIfChild(path) ?: run {
                Timber.e(
                    "The requested file: %s is outside the mounted directory: %s",
                    path, viewModel.webRoot
                )
                return@handler notFoundResponse
            }

            if (!file.exists() && viewModel.config.historyFallback) {
                val historyFallbackFile =
                    SuFile(viewModel.webRoot, viewModel.config.historyFallbackFile)

                return@handler historyFallbackFile.asResponse()
            }

            val injections = buildList {
                if (prefs.enableErudaConsole) {
                    addInjection({
                        appendLine("<!-- MMRL Eruda Inject -->")
                        appendLine("<script type=\"module\">")
                        appendLine("\timport eruda from \"https://mui.kernelsu.org/mmrl/assets/eruda.mjs\";")
                        appendLine("\teruda.init();")
                        appendLine("\tconst sheet = new CSSStyleSheet();")
                        appendLine("\tsheet.replaceSync(\".eruda-dev-tools { padding-bottom: ${insets.bottom}px }\");")
                        appendLine("\twindow.eruda.shadowRoot.adoptedStyleSheets.push(sheet)")
                        appendLine("</script>")
                    })
                }

                if (customCssFile.exists()) {
                    addInjection({
                        appendLine("<!-- MMRL Custom Stylesheet Inject -->")
                        appendLine("<link rel=\"stylesheet\" href=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.css\" type=\"text/css\" />")
                    })
                }

                if (customJsFile.exists()) {
                    addInjection({
                        appendLine("<!-- MMRL Custom JavaScript Inject -->")
                        appendLine("<script src=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.js\" type=\"module\"></script>")
                    }, InjectionType.BODY)
                }

                addInjection(insets.cssInject)
            }

            return@handler file.asResponse(injections)
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            return@handler notFoundResponse
        }
    }
}