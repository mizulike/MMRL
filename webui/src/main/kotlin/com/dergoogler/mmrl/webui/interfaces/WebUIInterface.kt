package com.dergoogler.mmrl.webui.interfaces

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView

interface WebUIConsole {
    fun error(message: String)
    fun info(message: String)
    fun log(message: String)
    fun warn(message: String)
}

open class WebUIInterface(
    val webView: WebView,
    val context: Context,
) {
    val activity = context as Activity
    fun runOnUiThread(block: () -> Unit) = (context as Activity).runOnUiThread(block)
    fun runJs(script: String) = runOnUiThread { webView.evaluateJavascript(script, null) }
    fun runPost(action: WebView.() -> Unit) {
        webView.post { action(webView) }
    }

    fun runJsCatching(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            runJs("new Error('${e.message}', { cause: '${e.message}' })")
        }
    }

    fun runMainLooperPost(action: () -> Unit) {
        if (context is Activity) {
            Handler(Looper.getMainLooper()).post(action)
        }
    }

    val console = object : WebUIConsole {
        override fun error(message: String) = runJs("console.error('$message')")
        override fun info(message: String) = runJs("console.info('$message')")
        override fun log(message: String) = runJs("console.log('$message')")
        override fun warn(message: String) = runJs("console.warn('$message')")
    }

    fun <R> runTry(
        message: String = "Unknown Error",
        default: R,
        block: () -> R,
    ): R = try {
        block()
    } catch (e: Throwable) {
        runJs("new Error('$message', { cause: \"${e.message}\" })")
        default
    }

    fun <R> runTry(
        message: String = "Unknown Error",
        block: () -> R,
    ): R? = runTry(message, null, block)

    fun <R, T> runTryJsWith(
        with: T,
        message: String = "Unknown Error",
        block: T.() -> R,
    ): R? = runTryJsWith(with, message, null, block)

    fun <R, T> runTryJsWith(
        with: T,
        message: String = "Unknown Error",
        default: R,
        block: T.() -> R,
    ): R {
        return try {
            with(with, block)
        } catch (e: Throwable) {
            runJs("new Error('$message', { cause: \"${e.message}\" })")
            return default
        }
    }
}