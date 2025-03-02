package com.dergoogler.mmrl.ui.activity.webui.handlers

import android.webkit.WebResourceResponse
import androidx.annotation.WorkerThread
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.dergoogler.mmrl.ui.activity.webui.MimeUtil.getMimeFromFileName
import com.dergoogler.mmrl.utils.file.SuFile
import dev.dergoogler.mmrl.compat.core.BrickException
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class SuFilePathHandler(
    directory: SuFile,
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
                Timber.tag(TAG).e(
                    "The requested file: %s is outside the mounted directory: %s",
                    path, mDirectory
                )
                return null
            }

            WebResourceResponse(guessMimeType(path), null, openFile(file))
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Error opening the requested path: %s", path)
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

    private fun openFile(file: SuFile): InputStream? {
        if (!file.exists()) {
            Timber.tag(TAG).e("File not found: %s", file.absolutePath)
            return null
        }

        val byteFile = file.readBytes()
        val `is` = ByteArrayInputStream(byteFile)
        return handleSvgzStream(file.path, `is`)
    }

    private fun guessMimeType(filePath: String): String {
        val mimeType = getMimeFromFileName(filePath)
        return mimeType ?: DEFAULT_MIME_TYPE
    }

    companion object {
        private const val TAG = "SuFilePathHandler"

        private const val DEFAULT_MIME_TYPE: String = "text/plain"

        private val FORBIDDEN_DATA_DIRS = arrayOf("/data/data", "/data/system")
    }
}