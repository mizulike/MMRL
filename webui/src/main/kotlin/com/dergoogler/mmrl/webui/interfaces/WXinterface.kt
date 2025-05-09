package com.dergoogler.mmrl.webui.interfaces

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.dergoogler.mmrl.platform.model.ModId

interface WXConsole {
    fun error(message: String, vararg args: String?)
    fun info(message: String, vararg args: String?)
    fun log(message: String, vararg args: String?)
    fun warn(message: String, vararg args: String?)
}

data class WXOptions(
    val context: Context,
    val webView: WebView,
    val modId: ModId,
)

/**
 * `WXIInterface` is an abstract base class that provides a set of common utilities for interacting with a WebView within a web-based user interface.
 *
 * It offers functionalities for running JavaScript code, handling asynchronous tasks, logging, error handling, and managing deprecation warnings.
 * This class is designed to be extended by specific WebUI interfaces, providing them with a standardized way to communicate with the web view and the underlying Android environment.
 *
 * @param wxOptions An instance of [WXOptions] containing essential configuration details such as the application context, WebView instance, and module identifier.
 *
 * @property context The Android context associated with the web UI. Typically, this is an instance of [Activity].
 * @property webView The [WebView] instance used for displaying web content.
 * @property modId A unique identifier for the module using this interface.
 * @property name The name of the entity. Must be initialized before access.
 * @property activity The [Activity] instance from context.
 * @property console An object implementing the [WXConsole] interface, offering logging capabilities.
 */
open class WXInterface(
    val wxOptions: WXOptions,
) {
    val context = wxOptions.context
    val webView = wxOptions.webView
    val modId = wxOptions.modId

    /**
     * The name of the entity.
     *
     * This property holds the name associated with this object.
     * It is declared as `lateinit` which means it must be initialized before being accessed.
     * Attempting to access it before initialization will result in a [kotlin.UninitializedPropertyAccessException].
     */
    open lateinit var name: String
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

    /**
     * Logs a deprecation warning to the console, indicating that a specific method will be removed in future versions.
     *
     * This function provides a standardized way to notify users about deprecated methods, guiding them towards replacements if available.
     *
     * @param method The name of the deprecated method. This will be included in the warning message.
     * @param replaceWith Optional. The name of the method that should be used as a replacement for the deprecated one.
     *                    If provided, the warning message will include instructions on what to use instead.
     *
     * @sample
     * // Deprecating a function named "oldFunction" without a replacement.
     * deprecated("oldFunction")
     *
     * // Deprecating a function named "oldFunction" and suggesting "newFunction" as a replacement.
     * deprecated("oldFunction", "newFunction")
     */
    fun deprecated(method: String, replaceWith: String? = null) {
        console.log(
            "%c[DEPRECATED]%c The `$method` method will be removed in future versions.${if (replaceWith != null) " Use `$replaceWith` instead." else ""}",
            "color: white; background: red; font-weight: bold; padding: 2px 6px; border-radius: 4px;",
            "color: orange; font-weight: bold;"
        );
    }

    val console = object : WXConsole {
        private val String.escape get() = this.replace("'", "\\'")

        private fun levelParser(level: String, message: String, vararg args: String?) =
            runJs(
                "console.$level('${message.escape}'${
                    args.joinToString(
                        prefix = if (args.isNotEmpty()) ", " else "",
                        separator = ", "
                    ) { "'${it?.escape}'" }
                })")

        override fun error(message: String, vararg args: String?) =
            levelParser("error", message, *args)

        override fun info(message: String, vararg args: String?) =
            levelParser("info", message, *args)

        override fun log(message: String, vararg args: String?) =
            levelParser("log", message, *args)

        override fun warn(message: String, vararg args: String?) =
            levelParser("warn", message, *args)
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