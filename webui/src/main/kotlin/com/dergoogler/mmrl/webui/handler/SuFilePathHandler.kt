package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.annotation.WorkerThread
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.webui.InjectionType
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.addInjection
import com.dergoogler.mmrl.webui.asResponse
import com.dergoogler.mmrl.webui.forbiddenResponse
import com.dergoogler.mmrl.webui.notFoundResponse
import com.dergoogler.mmrl.webui.util.WebUIOptions
import java.io.IOException
import kotlin.text.appendLine

class SuFilePathHandler : PathHandler {
    private val mDirectory: SuFile
    private val mOptions: WebUIOptions
    private val mInsets: Insets

    constructor(
        options: WebUIOptions,
        directory: SuFile,
        insets: Insets
    ) {
        try {
            mOptions = options
            mInsets = insets
            mDirectory = SuFile(directory).getCanonicalDirPath().toSuFile()
            require(isAllowedInternalStorageDir()) {
                ("The given directory \"" + directory
                        + "\" doesn't exist under an allowed app internal storage directory")
            }
        } catch (e: IOException) {
            throw IllegalArgumentException(
                "Failed to resolve the canonical path for the given directory: "
                        + directory.getPath(), e
            )
        }
    }

    @Throws(IOException::class)
    private fun isAllowedInternalStorageDir(): Boolean {
        val dir = mDirectory.getCanonicalDirPath()

        for (forbiddenPath in FORBIDDEN_DATA_DIRS) {
            if (dir.startsWith(forbiddenPath)) {
                return false
            }
        }
        return true
    }

    val configBase get() = SuFile("/data/adb/.config/${mOptions.modId.id}")
    val configStyleBase get() = SuFile(configBase, "style")
    val configJsBase get() = SuFile(configBase, "js")

    val customCssFile get() = SuFile(configStyleBase, "custom.css")

    val customJsHead get() = SuFile(configJsBase, "head")
    val customJsBody get() = SuFile(configJsBase, "body")
    val customJsFile get() = SuFile(customJsBody, "custom.js")


    val reversedPaths get() =
        listOf("mmrl/", "internal/", "favicon.ico")

    @WorkerThread
    override fun handle(path: String): WebResourceResponse? {
        reversedPaths.forEach {
            if (path.startsWith(it)) return null
        }

        if (mOptions.debug) Log.d(TAG, "handle: $path")

        try {
            val file = mDirectory.getCanonicalFileIfChild(path) ?: run {
                Log.e(
                    TAG,
                    "The requested file: %s is outside the mounted directory: %s".format(
                        path,
                        mOptions.webRoot
                    ),
                )
                return notFoundResponse
            }

            if (!file.exists() && mOptions.config.historyFallback) {
                val historyFallbackFile =
                    SuFile(
                        mOptions.webRoot,
                        mOptions.config.historyFallbackFile
                    )

                return historyFallbackFile.asResponse()
            }

            if (path.startsWith("__root__") && !mOptions.config.hasRootPathPermission) {
                return forbiddenResponse
            }

            data class Editor(
                val mode: String,
                val name: String,
                val file: SuFile,
            )

            val injections = buildList {
                if (mOptions.isErudaEnabled) {
                    addInjection {
                        appendLine("<script data-internal src=\"https://mui.kernelsu.org/internal/assets/eruda/eruda-editor.js\"></script>")
                        appendLine("<script data-internal type=\"module\">")
                        appendLine("\timport eruda from \"https://mui.kernelsu.org/internal/assets/eruda/eruda.mjs\";")
                        appendLine("\teruda.init();")

                        val editors = listOf(
                            Editor("css", "style", customCssFile),
                            Editor("javascript", "script", customJsFile),
                        )

                        for (editor in editors) {
                            appendLine("\tconst ${editor.name} = erudaEditor({")
                            appendLine("\t\t\tmodId: \"${mOptions.modId.id}\",")
                            appendLine("\t\t\tfile: ${mOptions.modId.sanitizedIdWithFile},")
                            appendLine("\t\t\tfileToEdit: \"${editor.file.path}\",")
                            appendLine("\t\t\tlang: \"${editor.mode}\",")
                            appendLine("\t\t\tname: \"${editor.name}\",")
                            appendLine("\t})")
                            appendLine("eruda.add(${editor.name})")
                        }

                        appendLine("\tconst sheet = new CSSStyleSheet();")
                        appendLine("\tsheet.replaceSync(\".eruda-dev-tools { padding-bottom: ${mInsets.bottom}px }\");")
                        appendLine("\twindow.eruda.shadowRoot.adoptedStyleSheets.push(sheet)")
                        appendLine("</script>")
                    }
                }

                if (mOptions.config.autoStatusBarsStyle) {
                    addInjection {
                        appendLine("<script data-internal-configurable type=\"module\">")
                        appendLine("$${mOptions.modId.sanitizedId}.setLightStatusBars(!$${mOptions.modId.sanitizedId}.isDarkMode())")
                        appendLine("</script>")
                    }
                }

                configStyleBase.exists {
                    it.listFiles { f -> f.exists() && f.extension == "css" }.forEach {
                        addInjection {
                            appendLine("<link data-internal rel=\"stylesheet\" href=\"https://mui.kernelsu.org/.adb/.config/${mOptions.modId.id}/style/${it.name}\" type=\"text/css\" />")
                        }
                    }
                }

                addInjection(InjectionType.BODY) {
                    appendLine("<script data-internal data-internal-dont-use src=\"https://mui.kernelsu.org/internal/scripts/require.js\" type=\"module\"></script>")
                }

                addInjection(InjectionType.BODY) {
                    appendLine("<script data-internal data-internal-dont-use src=\"https://mui.kernelsu.org/internal/scripts/sufile-fetch-ext.js\" type=\"module\"></script>")
                }

                customJsHead.exists {
                    it.listFiles { f -> f.exists() && f.extension == "js" }.forEach {
                        addInjection(InjectionType.HEAD) {
                            appendLine("<script data-internal src=\"https://mui.kernelsu.org/.adb/.config/${mOptions.modId.id}/js/head/${it.name}\" type=\"module\"></script>")
                        }
                    }
                }

                customJsBody.exists {
                    it.listFiles { f -> f.exists() && f.extension == "js" }.forEach {
                        addInjection(InjectionType.BODY) {
                            appendLine("<script data-internal src=\"https://mui.kernelsu.org/.adb/.config/${mOptions.modId.id}/js/body/${it.name}\" type=\"module\"></script>")
                        }
                    }
                }

                addInjection(mInsets.cssInject)
            }

            val fileExt = file.extension

            if (fileExt == "html" || fileExt == "htm") {
                return file.asResponse(injections)
            }

            return file.asResponse()
        } catch (e: IOException) {
            Log.e(TAG, "Error opening the requested path: $path", e)
            return notFoundResponse
        }

        return notFoundResponse
    }

    companion object {
        private const val TAG = "SuFilePathHandler"
        private val FORBIDDEN_DATA_DIRS: Array<String> =
            arrayOf<String>("/data/data", "/data/system")
    }
}