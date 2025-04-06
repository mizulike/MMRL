package com.dergoogler.webui.handlers

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
import com.dergoogler.webui.core.PathHandler
import com.dergoogler.webui.core.addInjection
import com.dergoogler.webui.core.asResponse
import com.dergoogler.webui.core.notFoundResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun webrootPathHandler(
    viewModel: WebUIViewModel,
): PathHandler {
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
        if (path.startsWith("mmrl/")) return@handler null
        if (path.startsWith(".adb/")) return@handler null
        if (path.startsWith(".${viewModel.modId}/")) return@handler null
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

            data class Editor(
                val mode: String,
                val name: String,
                val file: SuFile,
            )

            val injections = buildList {
                if (prefs.developerMode && prefs.enableErudaConsole) {
                    addInjection({
                        appendLine("<script data-mmrl src=\"https://mui.kernelsu.org/mmrl/assets/eruda/eruda-editor.js\"></script>")
                        appendLine("<script data-mmrl type=\"module\">")
                        appendLine("\timport eruda from \"https://mui.kernelsu.org/mmrl/assets/eruda/eruda.mjs\";")
                        appendLine("\teruda.init();")

                        val editors = listOf(
                            Editor("css", "style", customCssFile),
                            Editor("javascript", "script", customJsFile),
                        )

                        for (editor in editors) {
                            appendLine("\tconst ${editor.name} = erudaEditor({")
                            appendLine("\t\t\tmodId: \"${viewModel.modId}\",")
                            appendLine("\t\t\tfile: ${viewModel.sanitizedModIdWithFile},")
                            appendLine("\t\t\tfileToEdit: \"${editor.file.path}\",")
                            appendLine("\t\t\tlang: \"${editor.mode}\",")
                            appendLine("\t\t\tname: \"${editor.name}\",")
                            appendLine("\t})")
                            appendLine("eruda.add(${editor.name})")
                        }

                        appendLine("\tconst sheet = new CSSStyleSheet();")
                        appendLine("\tsheet.replaceSync(\".eruda-dev-tools { padding-bottom: ${insets.bottom}px }\");")
                        appendLine("\twindow.eruda.shadowRoot.adoptedStyleSheets.push(sheet)")
                        appendLine("</script>")
                    })
                }

                if (customCssFile.exists()) {
                    addInjection({
                        appendLine("<link data-mmrl rel=\"stylesheet\" href=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.css\" type=\"text/css\" />")
                    })
                }

                addInjection({
                    appendLine("<script data-mmrl-internal data-mmrl-dont-use src=\"https://mui.kernelsu.org/mmrl/scripts/require.js\" type=\"module\"></script>")
                }, InjectionType.BODY)

                addInjection({
                    appendLine("<script data-mmrl-internal data-mmrl-dont-use src=\"https://mui.kernelsu.org/mmrl/scripts/sufile-fetch-ext.js\" type=\"module\"></script>")
                }, InjectionType.BODY)

                if (customJsFile.exists()) {
                    addInjection({
                        appendLine("<script data-mmrl src=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/custom.js\" type=\"module\"></script>")
                    }, InjectionType.BODY)
                }

                addInjection(insets.cssInject)
            }

            return@handler file.asResponse(injections)
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            return@handler null
        }
    }
}