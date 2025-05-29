package com.dergoogler.mmrl.webui

import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.platform.file.SuFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

typealias PathHandler = (String) -> WebResourceResponse?
typealias WXAssetLoader = (Uri) -> WebResourceResponse?

data class PathHandleData(
    val authority: String,
    val path: String,
    val httpEnabled: Boolean = false,
    val handle: PathHandler,
) {
    companion object {
        const val HTTP_SCHEME: String = "http"
        const val HTTPS_SCHEME: String = "https"
    }

    @WorkerThread
    fun match(uri: Uri): PathHandler? {
        if (uri.scheme == HTTP_SCHEME && !httpEnabled) {
            return null
        }

        if (uri.scheme != HTTP_SCHEME && uri.scheme != HTTPS_SCHEME) {
            return null
        }

        if (uri.authority != authority) {
            return null
        }

        if (!uri.path!!.startsWith(path)) {
            return null
        }

        return handle
    }

    @WorkerThread
    fun getSuffixPath(url: String) = url.replaceFirst(this.path, "")
}

@WorkerThread
fun wxAssetLoader(
    domain: String = "mui.kernelsu.org",
    httpEnabled: Boolean = false,
    handlers: List<Pair<String, PathHandler>> = emptyList(),
): WXAssetLoader {
    val matchers = handlers.map { (path, handler) ->
        PathHandleData(
            authority = domain,
            path = path,
            httpEnabled = httpEnabled,
            handle = handler
        )
    }

    return HOLY_FUCK_MAN@ @WorkerThread { uri: Uri ->
        for (matcher in matchers) {
            val handler = matcher.match(uri) ?: continue
            // The requested URL doesn't match the URL where this handler has been registered.
            val suffixPath = matcher.getSuffixPath(uri.path!!)
            val response = handler(suffixPath) ?: continue
            // Handler doesn't want to intercept this request, try next handler.
            return@HOLY_FUCK_MAN response
        }

        return@HOLY_FUCK_MAN null
    }

}

fun InputStream.inject(fromTag: (ByteArray) -> Int, code: String): InputStream {
    val cssBytes = code.toByteArray()

    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (this.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }

    val modifiedHtml = outputStream.toByteArray()
    val index = fromTag(modifiedHtml)

    return if (index != -1) {
        ByteArrayInputStream(
            modifiedHtml.copyOfRange(
                0,
                index
            ) + cssBytes + modifiedHtml.copyOfRange(index, modifiedHtml.size)
        )
    } else {
        ByteArrayInputStream(modifiedHtml)
    }
}

fun InputStream.headInject(code: String): InputStream = inject(::findHeadTag, code)
fun InputStream.bodyInject(code: String): InputStream = inject(::findBodyTag, code)

private fun findHeadTag(htmlBytes: ByteArray): Int {
    val headTag = "</head>".toByteArray()
    for (i in 0..htmlBytes.size - headTag.size) {
        if (htmlBytes.copyOfRange(i, i + headTag.size).contentEquals(headTag)) {
            return i
        }
    }
    return -1
}

private fun findBodyTag(htmlBytes: ByteArray): Int {
    val bodyTag = "</body>".toByteArray()
    for (i in 0..htmlBytes.size - bodyTag.size) {
        if (htmlBytes.copyOfRange(i, i + bodyTag.size).contentEquals(bodyTag)) {
            return i
        }
    }
    return -1
}

@Throws(IOException::class)
fun SuFile.handleSvgzStream(
    stream: InputStream,
): InputStream {
    return if (extension === "svgz") GZIPInputStream(stream) else stream
}

fun String.asStyleResponse(): WebResourceResponse {
    val inputStream: InputStream =
        ByteArrayInputStream(this.toByteArray(StandardCharsets.UTF_8))

    return WebResourceResponse(
        "text/css",
        "UTF-8",
        inputStream
    )
}

fun String.asScriptResponse(): WebResourceResponse {
    val inputStream: InputStream =
        ByteArrayInputStream(this.toByteArray(StandardCharsets.UTF_8))

    return WebResourceResponse(
        "text/javascript",
        "UTF-8",
        inputStream
    )
}
