package com.dergoogler.mmrl.ui.activity.webui.handlers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.ui.activity.webui.WebRootUtil
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import dev.dergoogler.mmrl.compat.core.MMRLPathHandler
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

class WebRootPathHandler(
    private val viewModel: WebUIViewModel,
    directory: SuFile = viewModel.webRoot,
) : MMRLPathHandler(directory, listOf("mmrl/", ".adb/")) {
    private val allowInjectEruda =
        viewModel.modId in viewModel.userPrefs.injectEruda

    private val customCssFile = SuFile(viewModel.webRoot, "custom.mmrl.css")

    @Throws(IOException::class)
    override fun openFile(file: SuFile): InputStream? {
        if (!file.exists() && viewModel.config.historyFallback) {
            val historyFallbackFile = SuFile(directory, viewModel.config.historyFallbackFile)
            return handleSvgzStream(
                historyFallbackFile.path,
                historyFallbackFile.newInputStream()
            )
        }
        if (!file.exists()) {
            Timber.e("File not found: %s", file.absolutePath)
            return null
        }

        if (guessMimeType(file.path) != "text/html") {
            return handleSvgzStream(file.path, file.newInputStream())
        }

        var html by mutableStateOf(file.newInputStream())

        if (allowInjectEruda) {
            val code = """
<!-- Start MMRL Inject -->
<script type="module">
    import eruda from "https://mui.kernelsu.org/mmrl/assets/eruda.mjs"; 
    eruda.init();
    const sheet = new CSSStyleSheet();
    sheet.replaceSync('.eruda-dev-tools { padding-bottom: ${viewModel.bottomInset}px }');
    window.eruda.shadowRoot.adoptedStyleSheets.push(sheet)
</script>
<!-- End MMRL Inject -->
        """.trimIndent()

            html = injectCode(code, html)
        }

        if (customCssFile.exists()) {
            val code =
                "<link rel=\"stylesheet\" href=\"https://mui.kernelsu.org/custom.mmrl.css\" type=\"text/css\">"
            html = injectCode(code, html)
        }

        val insets = WebRootUtil.cssInsets(
            viewModel.topInset,
            viewModel.bottomInset,
            viewModel.leftInset,
            viewModel.rightInset
        )

        html = injectCode(insets, html)

        return handleSvgzStream(file.path, html)
    }
}