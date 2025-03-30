package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.mmrl.utils.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.core.LocalInsets
import com.dergoogler.webui.core.MimeUtil
import com.dergoogler.webui.core.asResponse
import com.dergoogler.webui.core.bodyInject
import com.dergoogler.webui.core.headInject
import com.dergoogler.webui.core.noResponse
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
        if (path.startsWith("mmrl/")) return@handler noResponse
        if (path.startsWith(".adb/")) return@handler noResponse
        if (path.startsWith(".${viewModel.modId}/")) return@handler noResponse
        if (path.endsWith("favicon.ico") || path.startsWith("favicon.ico")) return@handler noResponse

        try {
            val file = directory.getCanonicalFileIfChild(path) ?: run {
                Timber.e(
                    "The requested file: %s is outside the mounted directory: %s",
                    path, viewModel.webRoot
                )
                return@handler noResponse
            }

            Timber.d("file: ${file.absolutePath}")

            if (!file.exists() && viewModel.config.historyFallback) {
                val historyFallbackFile =
                    SuFile(viewModel.webRoot, viewModel.config.historyFallbackFile)

                return@handler historyFallbackFile.asResponse()
            }

            if (!file.exists()) {
                Timber.e("File not found: %s", file.absolutePath)
                return@handler noResponse
            }


            val mimeType = MimeUtil.getMimeFromFileName(path)

            var html by mutableStateOf(file.newInputStream())

            if (prefs.enableErudaConsole) {
                html = html.headInject(buildString {
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
                html = html.headInject(buildString {
                    appendLine("<!-- MMRL Custom Stylesheet Inject -->")
                    appendLine("<link rel=\"stylesheet\" href=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.css\" type=\"text/css\" />")
                })
            }

            if (customJsFile.exists()) {
                html = html.bodyInject(buildString {
                    appendLine("<!-- MMRL Custom JavaScript Inject -->")
                    appendLine("<script src=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.js\" type=\"module\"></script>")
                })
            }


            html = html.headInject(insets.cssInject)


            WebResourceResponse(mimeType, null, html)
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            return@handler noResponse
        }
    }
}