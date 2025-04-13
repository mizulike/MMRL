package com.dergoogler.mmrl.webui.interfaces

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.dergoogler.mmrl.webui.moshi
import java.io.BufferedInputStream
import java.io.InputStream

class FileInputInterface(
    webView: WebView,
    context: Context,
) : WebUIInterface(webView, context) {
    @JavascriptInterface
    fun open(path: String): FileInputInterfaceStream? =
        try {
            val file = com.dergoogler.mmrl.platform.file.SuFile(path)
            val inputStream = file.newInputStream()

            FileInputInterfaceStream(inputStream, webView, context)
        } catch (e: Exception) {
            null
        }
}

class FileInputInterfaceStream(
    inputStream: InputStream,
    webView: WebView,
    context: Context,
) : WebUIInterface(webView, context) {
    private val bufferedInputStream = BufferedInputStream(inputStream)

    @JavascriptInterface
    fun read(): Int = runTry("Error while reading from stream", -1) {
        bufferedInputStream.read()
    }

    @JavascriptInterface
    fun readChunk(chunkSize: Int): String? {
        val buffer = ByteArray(chunkSize)
        val bytesRead = bufferedInputStream.read(buffer)
        return if (bytesRead > 0) {
            moshi.adapter(ByteArray::class.java).toJson(buffer.copyOf(bytesRead))
        } else {
            null
        }
    }

    @JavascriptInterface
    fun close() = runTry("Error while closing stream") {
        bufferedInputStream.close()
    }

    @JavascriptInterface
    fun skip(n: Long): Long = runTry("Error while skipping $n bytes", -1) {
        bufferedInputStream.skip(n)
    }
}