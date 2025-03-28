package dev.dergoogler.mmrl.compat.core

import android.webkit.WebResourceResponse
import androidx.annotation.WorkerThread
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.dergoogler.mmrl.ui.activity.webui.MimeUtil.getMimeFromFileName
import com.dergoogler.mmrl.utils.file.SuFile
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

open class MMRLPathHandler(
    internal var directory: SuFile,
    private val ignorePaths: List<String> = listOf("mmrl/"),
) : PathHandler {

    init {
        try {
            directory = SuFile(getCanonicalDirPath(directory))
            if (!isAllowedInternalStorageDir()) {
                throw BrickException(
                    message = "The given directory \"$directory\" doesn't exist under an allowed app internal storage directory",
                    helpMessage = "Some directories are not allowed like **/data/data** and **/data/system**."
                )
            }
        } catch (e: IOException) {
            throw BrickException(
                message = "Failed to resolve the canonical path for the given directory: ${directory.path}",
                helpMessage = "Did you checked that the given directory exists?",
                cause = e
            )
        }
    }

    @Throws(IOException::class)
    internal fun isAllowedInternalStorageDir(): Boolean {
        val dir = getCanonicalDirPath(directory)

        for (forbiddenPath: String in FORBIDDEN_DATA_DIRS) {
            if (dir.startsWith(forbiddenPath)) {
                return false
            }
        }
        return true
    }

    @WorkerThread
    override fun handle(path: String): WebResourceResponse? {
        for (ignoredPath: String in ignorePaths) {
            if (path.startsWith(ignoredPath)) {
                return null
            }
        }

        return try {
            val file = getCanonicalFileIfChild(directory, path) ?: run {
                Timber.e(
                    "The requested file: %s is outside the mounted directory: %s",
                    path, directory
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
    internal fun handleSvgzStream(
        path: String,
        stream: InputStream,
    ): InputStream {
        return if (path.endsWith(".svgz")) GZIPInputStream(stream) else stream
    }

    @Throws(IOException::class)
    internal open fun openFile(file: SuFile): InputStream? {
        if (!file.exists()) {
            Timber.e("File not found: %s", file.absolutePath)
            return null
        }

        return handleSvgzStream(file.path, file.newInputStream())
    }

    internal fun guessMimeType(filePath: String): String {
        val mimeType = getMimeFromFileName(filePath)
        return mimeType ?: DEFAULT_MIME_TYPE
    }

    internal fun injectCode(code: String, inputStream: InputStream): InputStream {
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
        const val DEFAULT_MIME_TYPE: String = "text/plain"
        val FORBIDDEN_DATA_DIRS = arrayOf("/data/data", "/data/system")
    }
}