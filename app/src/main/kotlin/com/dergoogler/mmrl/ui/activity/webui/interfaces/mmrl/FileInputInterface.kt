package com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.dergoogler.mmrl.utils.file.SuFile
import dev.dergoogler.mmrl.compat.core.MMRLWebUIInterface
import java.io.InputStream

class FileInputInterface(
    webView: WebView,
    context: Context,
) : MMRLWebUIInterface(webView, context) {
    @JavascriptInterface
    fun open(path: String): FileInputInterfaceStream {
        val file = SuFile(path)
        val inputStream = file.newInputStream()

        return FileInputInterfaceStream(inputStream, webView, context)
    }
}

class FileInputInterfaceStream(
    private val inputStream: InputStream,
    webView: WebView,
    context: Context,
) : MMRLWebUIInterface(webView, context) {
    @JavascriptInterface
    fun read(): Int = runTry("Error while reading from stream", -1) { inputStream.read() }

    @JavascriptInterface
    fun close() = runTry("Error while closing stream") { inputStream.close() }

    @JavascriptInterface
    fun skip(n: Long): Long = runTry("Error while skipping $n bytes", -1) { inputStream.skip(n) }
}