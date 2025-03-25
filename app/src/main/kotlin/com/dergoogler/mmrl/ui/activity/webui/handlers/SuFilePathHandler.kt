package com.dergoogler.mmrl.ui.activity.webui.handlers

import android.webkit.WebResourceResponse
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.dergoogler.mmrl.ui.activity.webui.MimeUtil.getMimeFromFileName
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import dev.dergoogler.mmrl.compat.core.BrickException
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class SuFilePathHandler(
    private val viewModel: WebUIViewModel,
    directory: SuFile = viewModel.webRoot,
) : PathHandler {
    private var mDirectory: SuFile

    init {
        try {
            mDirectory = SuFile(getCanonicalDirPath(directory))
            if (!isAllowedInternalStorageDir()) {
                throw BrickException(
                    msg = "The given directory \"$directory\" doesn't exist under an allowed app internal storage directory",
                    helpMessage = "Some directories are not allowed like **/data/data** and **/data/system**."
                )
            }
        } catch (e: IOException) {
            throw IllegalArgumentException(
                ("Failed to resolve the canonical path for the given directory: "
                        + directory.path), e
            )
        }
    }

    @Throws(IOException::class)
    private fun isAllowedInternalStorageDir(): Boolean {
        val dir = getCanonicalDirPath(mDirectory)

        for (forbiddenPath: String in FORBIDDEN_DATA_DIRS) {
            if (dir.startsWith(forbiddenPath)) {
                return false
            }
        }
        return true
    }

    @WorkerThread
    override fun handle(path: String): WebResourceResponse? {
        if (path.startsWith("mmrl/") || path.startsWith("favicon.ico")) return null

        return try {
            val file = getCanonicalFileIfChild(mDirectory, path) ?: run {
                Timber.e(
                    "The requested file: %s is outside the mounted directory: %s",
                    path, mDirectory
                )
                return null
            }

            WebResourceResponse(guessMimeType(path), null, openFile(file))
        } catch (e: IOException) {
            Timber.e(e, "Error opening the requested path: %s", path)
            null
        }
    }

    @Throws(IOException::class)
    fun getCanonicalDirPath(file: SuFile): String {
        var canonicalPath = file.canonicalPath
        if (!canonicalPath.endsWith("/")) canonicalPath += "/"
        return canonicalPath
    }

    @Throws(IOException::class)
    fun getCanonicalFileIfChild(parent: SuFile, child: String): SuFile? {
        val parentCanonicalPath = getCanonicalDirPath(parent)
        val childCanonicalPath = SuFile(parent, child).canonicalPath
        if (childCanonicalPath.startsWith(parentCanonicalPath)) {
            return SuFile(childCanonicalPath)
        }
        return null
    }

    @Throws(IOException::class)
    private fun handleSvgzStream(
        path: String,
        stream: InputStream,
    ): InputStream {
        return if (path.endsWith(".svgz")) GZIPInputStream(stream) else stream
    }

    private val allowInjectEruda =
        viewModel.config.hasErudaPermission && viewModel.modId in viewModel.userPrefs.injectEruda

    private fun openFile(file: SuFile): InputStream? {
        if (!file.exists() && viewModel.config.historyFallback) {
            val historyFallbackFile = SuFile(mDirectory, viewModel.config.historyFallbackFile)
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

        return handleSvgzStream(file.path, html)
    }

    private fun guessMimeType(filePath: String): String {
        val mimeType = getMimeFromFileName(filePath)
        return mimeType ?: DEFAULT_MIME_TYPE
    }

    private fun injectCode(code: String, inputStream: InputStream): InputStream {
        val cssBytes = code.toByteArray()

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        val modifiedHtml = outputStream.toByteArray()
        val headIndex = findHeadTag(modifiedHtml)

        return if (headIndex != -1) {
            ByteArrayInputStream(
                modifiedHtml.copyOfRange(
                    0,
                    headIndex
                ) + cssBytes + modifiedHtml.copyOfRange(headIndex, modifiedHtml.size)
            )
        } else {
            ByteArrayInputStream(modifiedHtml)
        }
    }

    private fun findHeadTag(htmlBytes: ByteArray): Int {
        val headTag = "</head>".toByteArray()
        for (i in 0..htmlBytes.size - headTag.size) {
            if (htmlBytes.copyOfRange(i, i + headTag.size).contentEquals(headTag)) {
                return i
            }
        }
        return -1
    }

    companion object {
        private const val TAG = "SuFilePathHandler"

        private const val DEFAULT_MIME_TYPE: String = "text/plain"

        private val FORBIDDEN_DATA_DIRS = arrayOf("/data/data", "/data/system")
    }
}