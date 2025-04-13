package com.dergoogler.webui.handlers

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.webui.viewModel.WebUIViewModel
import com.dergoogler.mmrl.webui.InjectionType
import com.dergoogler.mmrl.webui.LocalInsets
import com.dergoogler.mmrl.webui.PathHandler
import com.dergoogler.mmrl.webui.addInjection
import com.dergoogler.mmrl.webui.asResponse
import com.dergoogler.mmrl.webui.notFoundResponse
import java.io.IOException

@Composable
fun webrootPathHandler(
    viewModel: WebUIViewModel,
): PathHandler {
    val insets = LocalInsets.current

    val configBase =
        SuFile("/data/adb/.config/${viewModel.modId}")
    val configStyleBase = SuFile(configBase, "style")
    val configJsBase = SuFile(configBase, "js")

    val customCssFile = SuFile(configStyleBase, "custom.css")

    val customJsHead = SuFile(configJsBase, "head")
    val customJsBody = SuFile(configJsBase, "body")
    val customJsFile = SuFile(customJsBody, "custom.js")

    val directory by remember {
        mutableStateOf(
            SuFile(viewModel.webRoot).getCanonicalDirPath().toSuFile()
        )
    }

    LaunchedEffect(Unit) {
        SuFile.createDirectories(customJsHead, customJsBody, configStyleBase)
    }

    val reversedPaths = listOf("mmrl/", ".adb/", ".local/", ".config/", ".${viewModel.modId}/")

    return handler@{ path ->
        reversedPaths.forEach {
            if (path.endsWith(it)) return@handler null
        }

        if (path.endsWith("favicon.ico") || path.startsWith("favicon.ico")) return@handler notFoundResponse

        try {
            val file = directory.getCanonicalFileIfChild(path) ?: run {
                Log.e(
                    "webrootPathHandler",
                    "The requested file: %s is outside the mounted directory: %s".format(
                        path,
                        viewModel.webRoot
                    ),
                )
                return@handler notFoundResponse
            }

            if (!file.exists() && viewModel.config.historyFallback) {
                val historyFallbackFile =
                    SuFile(
                        viewModel.webRoot,
                        viewModel.config.historyFallbackFile
                    )

                return@handler historyFallbackFile.asResponse()
            }

            data class Editor(
                val mode: String,
                val name: String,
                val file: SuFile,
            )

            val injections = buildList {
                if (viewModel.isErudaEnabled) {
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

                configStyleBase.exists {
                    it.listFiles { f -> f.exists() && f.extension == "css" }.forEach {
                        addInjection({
                            appendLine("<link data-mmrl rel=\"stylesheet\" href=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/style/${it.name}\" type=\"text/css\" />")
                        })
                    }
                }

                addInjection({
                    appendLine("<script data-mmrl-internal data-mmrl-dont-use src=\"https://mui.kernelsu.org/mmrl/scripts/require.js\" type=\"module\"></script>")
                }, InjectionType.BODY)

                addInjection({
                    appendLine("<script data-mmrl-internal data-mmrl-dont-use src=\"https://mui.kernelsu.org/mmrl/scripts/sufile-fetch-ext.js\" type=\"module\"></script>")
                }, InjectionType.BODY)

                customJsHead.exists {
                    it.listFiles { f -> f.exists() && f.extension == "js" }.forEach {
                        addInjection({
                            appendLine("<script data-mmrl src=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/js/head/${it.name}\" type=\"module\"></script>")
                        }, InjectionType.HEAD)
                    }
                }

                customJsBody.exists {
                    it.listFiles { f -> f.exists() && f.extension == "js" }.forEach {
                        addInjection({
                            appendLine("<script data-mmrl src=\"https://mui.kernelsu.org/.adb/.config/${viewModel.modId}/js/body/${it.name}\" type=\"module\"></script>")
                        }, InjectionType.BODY)
                    }
                }

                addInjection(insets.cssInject)
            }

            return@handler file.asResponse(injections)
        } catch (e: IOException) {
            Log.e("webrootPathHandler", "Error opening webroot path: $path", e)
            return@handler null
        }
    }
}