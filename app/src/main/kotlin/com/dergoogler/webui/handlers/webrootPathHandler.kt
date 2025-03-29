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
                val code = """
<!-- MMRL Eruda Inject -->
<script type="module">
    import eruda from "https://mui.kernelsu.org/mmrl/assets/eruda.mjs"; 
    eruda.init();
    const sheet = new CSSStyleSheet();
    sheet.replaceSync('.eruda-dev-tools { padding-bottom: ${insets.bottom}px }');
    window.eruda.shadowRoot.adoptedStyleSheets.push(sheet)
</script>
""".trimIndent()

                html = html.headInject(code)
            }

            if (customCssFile.exists()) {
                val code = """
<!-- MMRL Custom Stylesheet Inject -->
<link rel="stylesheet" href="https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.css" type="text/css" />
""".trimIndent()
                html = html.headInject(code)
            }

            if (customJsFile.exists()) {
                val code = """
<!-- MMRL Custom JavaScript Inject -->
<script src="https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.js" type="module"></script>
""".trimIndent()
                html = html.bodyInject(code)
            }


            html = html.headInject(insets.cssInject.trimIndent())


            WebResourceResponse(mimeType, null, html)
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            return@handler noResponse
        }
    }
}