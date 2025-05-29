package com.dergoogler.mmrl.webui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebMessage
import android.webkit.WebView
import androidx.annotation.UiThread
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.findActivity
import com.dergoogler.mmrl.ext.moshi.moshi
import com.dergoogler.mmrl.webui.interfaces.WXConsole
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.model.WXEventHandler
import com.dergoogler.mmrl.webui.util.WebUIOptions

/**
 * A custom WebView class that provides additional functionality for WebUI.
 *
 * This class extends the base WebView and adds features such as:
 * - Options management using [WebUIOptions].
 * - Simplified message posting to the WebView.
 * - Event handling for WebUI events.
 * - Utility functions for running JavaScript code and handling errors.
 * - Console logging integration.
 *
 * @property options The options for this WebUIView.
 * @property interfaces A set of JavaScript interface names that have been added to this WebView.
 * @property console A [WXConsole] implementation for logging messages from the WebView.
 */
@SuppressLint("ViewConstructor")
open class WebUIView(
    protected val options: WebUIOptions,
) : WebView(options.context) {
    internal var mSwipeView: WXSwipeRefresh? = null

    constructor(context: Context) : this(WebUIOptions(context = context)) {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
    }

    constructor(context: Context, attrs: AttributeSet) : this(WebUIOptions(context = context)) {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int,
    ) : this(WebUIOptions(context = context)) {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
    }

    protected fun createDefaultWxOptions(options: WebUIOptions): WXOptions = WXOptions(
        webView = this,
        options = options
    )

    protected val interfaces = hashSetOf<String>()

    fun <R> options(block: WebUIOptions.() -> R): R? {
        return try {
            block(options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get options:", e)
            null
        }
    }

    fun postMessage(message: String) {
        val uri = options.domain

        if (WebViewFeature.isFeatureSupported(WebViewFeature.POST_WEB_MESSAGE)) {
            val compatMessage = WebMessageCompat(message)
            WebViewCompat.postWebMessage(
                this,
                compatMessage,
                uri
            )
        } else {
            val baseMessage = WebMessage(message)
            super.postWebMessage(baseMessage, uri)
        }
    }

    fun postEventHandler(event: WXEventHandler) {
        val activity = context.findActivity()
        if (activity == null) {
            console.error("[$TAG] Activity not available for postEvent")
            return
        }

        val adapter = moshi.adapter(WXEventHandler::class.java)

        options {
            postMessage(
                adapter.toJson(event)
            )
        }
    }

    @UiThread
    fun runOnUiThread(block: Activity.() -> Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            console.error("[$TAG] Activity not found")
            return
        }

        block(activity)
    }

    @UiThread
    fun runJs(script: String) = runPost { evaluateJavascript(script, null) }

    @UiThread
    fun runPost(action: WebUIView.() -> Unit) {
        post { action(this) }
    }

    @UiThread
    fun throwJsError(e: Exception) = runJs("new Error('${e.message}', { cause: '${e.cause}' })")

    @UiThread
    fun runJsCatching(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            throwJsError(e)
        }
    }

    override fun destroy() {
        // remove all interfaces
        for (obj in interfaces) {
            removeJavascriptInterface(obj)
        }

        super.destroy()
    }

    open fun loadDomain() {
        this.loadUrl("${options.domain}/index.html")
    }

    /**
     * Adds a JavaScript interface to this WebView.
     *
     * This method overrides the default `addJavascriptInterface` method to keep track of added interfaces
     * and prevent duplicate additions.
     *
     * @param obj The object to be exposed as a JavaScript interface.
     * @param name The name of the JavaScript interface.
     */
    @SuppressLint("JavascriptInterface")
    override fun addJavascriptInterface(obj: Any, name: String) {
        if (name in interfaces) {
            Log.w(TAG, "Interface $name already exists")
            return
        }

        interfaces += name

        Log.d(TAG, "Added interface $name")

        super.addJavascriptInterface(obj, name)
    }

    /**
     * Adds a JavaScript interface to the WebView.
     *
     * This function takes a [JavaScriptInterface] object, creates a new instance of it
     * using the provided [WXOptions], and then adds it to the WebView using the
     * [addJavascriptInterface] method.
     *
     * @param obj The [JavaScriptInterface] object to add.
     * @throws BrickException if an error occurs while adding the interface.
     */
    @Throws(BrickException::class)
    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(obj: JavaScriptInterface<out WXInterface>) {
        try {
            val js = obj.createNew(createDefaultWxOptions(options))
            addJavascriptInterface(js.instance, js.name)
        } catch (e: Exception) {
            throw BrickException(
                message = "Couldn't add a new JavaScript interface.",
                cause = e,
            )
        }
    }

    /**
     * Adds multiple JavaScript interfaces to this WebView.
     *
     * This function iterates over the provided JavaScript interfaces and adds each one
     * to the WebView using the [WXOptions.addJavascriptInterface] method.
     *
     * @param obj A vararg of [JavaScriptInterface] objects to be added.
     * @throws BrickException If an error occurs while adding any of the JavaScript interfaces.
     * @see WXOptions.addJavascriptInterface
     */
    @Throws(BrickException::class)
    fun addJavascriptInterface(vararg obj: JavaScriptInterface<out WXInterface>) {
        obj.forEach { addJavascriptInterface(it) }
    }

    /**
     * A [WXConsole] implementation for logging messages from the WebView.
     *
     * This object provides methods for logging messages at different levels (error, info, log, warn)
     * by executing corresponding JavaScript `console` commands in the WebView.
     * It also handles escaping special characters in messages and arguments to prevent JavaScript errors.
     */
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

    companion object {
        private const val TAG = "WebUIView"
    }
}