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
 * @property mOptions The options for this WebUIView.
 * @property interfaces A set of JavaScript interface names that have been added to this WebView.
 * @property console A [WXConsole] implementation for logging messages from the WebView.
 */
open class WebUIView : WebView {
    protected val mOptions: WebUIOptions
    internal var mSwipeView: WXSwipeRefresh? = null

    constructor(options: WebUIOptions) : super(options) {
        this.mOptions = options
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.mOptions = createDefaultOptions() as WebUIOptions
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mOptions = createDefaultOptions() as WebUIOptions
    }

    protected val interfaces = hashSetOf<String>()


    fun <R> options(block: WebUIOptions.() -> R): R? {
        return try {
            block(mOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get options:", e)
            null
        }
    }

    fun postMessage(message: String) {
        val uri = mOptions.domain

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
            console.error("Activity not available for postEvent")
            return
        }

        val adapter = moshi.adapter(WXEventHandler::class.java)

        options {
            postMessage(
                adapter.toJson(event)
            )
        }
    }

    @Throws(UnsupportedOperationException::class)
    protected fun createDefaultOptions(): Any {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
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
    fun runJs(script: String) = runOnUiThread { evaluateJavascript(script, null) }

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
        this.loadUrl("${mOptions.domain}/index.html")
    }

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

    @Throws(BrickException::class)
    @SuppressLint("JavascriptInterface")
    fun WXOptions.addJavascriptInterface(obj: JavaScriptInterface<out WXInterface>) {
        try {
            val js = obj.createNew(this)
            addJavascriptInterface(js.instance, js.name)
        } catch (e: Exception) {
            throw BrickException(
                message = "Couldn't add a new JavaScript interface.",
                cause = e,
            )
        }
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

    companion object {
        private const val TAG = "WebUIView"
    }
}