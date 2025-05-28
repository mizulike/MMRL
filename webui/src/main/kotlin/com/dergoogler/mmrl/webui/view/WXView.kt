package com.dergoogler.mmrl.webui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup.LayoutParams
import android.view.WindowInsetsController
import android.webkit.WebMessage
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.UiThread
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.dergoogler.mmrl.ext.BuildCompat
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.moshi.moshi
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.webui.client.WXChromeClient
import com.dergoogler.mmrl.webui.client.WXClient
import com.dergoogler.mmrl.webui.interfaces.ApplicationInterface
import com.dergoogler.mmrl.webui.interfaces.FileInputInterface
import com.dergoogler.mmrl.webui.interfaces.FileInterface
import com.dergoogler.mmrl.webui.interfaces.ModuleInterface
import com.dergoogler.mmrl.webui.interfaces.PackageManagerInterface
import com.dergoogler.mmrl.webui.interfaces.UserManagerInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.model.WXEventHandler
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.util.getRequireNewVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WXView is a custom [WebView] component designed for the **WebUI X Engine**.
 * It provides enhanced functionality for web-based user interfaces within Android applications.
 *
 * This class handles the initialization of the WebView, including setting up JavaScript interfaces,
 * managing window insets, and configuring various WebView settings. It also provides helper
 * methods for interacting with the WebView from native code, such as posting messages,
 * handling events, and executing JavaScript.
 *
 * **Key Features:**
 * - **Simplified Initialization:**  Handles common WebView setup tasks automatically.
 * - **JavaScript Interface Management:**  Provides a structured way to add and manage JavaScript interfaces.
 * - **Window Inset Handling:**  Adjusts WebView content based on system window insets.
 * - **Event Handling:**  Facilitates communication between the WebView and native code through events.
 * - **Helper Methods:**  Offers convenient methods for common WebView operations.
 *
 * **Constructors:**
 * - `WXView(options: WebUIOptions)`: Initializes the WXView with the specified [WebUIOptions].
 *   This is the recommended constructor for creating a WXView instance.
 * - `WXView(context: Context, attrs: AttributeSet?)`:  Used for inflating WXView from XML layouts.
 *   **Note:** This constructor will throw an [UnsupportedOperationException] as default options are not supported.
 *   You must use the constructor with [WebUIOptions].
 * - `WXView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)`: Used for inflating WXView from XML layouts
 *   with a default style attribute.
 *   **Note:** This constructor will throw an [UnsupportedOperationException] as default options are not supported.
 *   You must use the constructor with [WebUIOptions].
 *
 * @property mOptions The [WebUIOptions] used to configure this WXView.
 * @property activity The [Activity] hosting this WXView, lazily initialized.
 * @property defaultWxOptions The default [WXOptions] created for this WXView.
 * @property mSwipeView An optional [SwipeRefreshLayout] that can be associated with this WXView.
 */
@SuppressLint("SetJavaScriptEnabled")
open class WXView : WebView {
    private val mOptions: WebUIOptions
    private val activity: Activity? by lazy { mOptions.findActivity() }
    private val scope = CoroutineScope(Dispatchers.Main)
    private var initJob: Job? = null
    private var isInitialized = false
    private val mDefaultWxOptions: WXOptions
    internal var mSwipeView: WXSwipeRefresh? = null

    private val interfaces = hashSetOf<String>()

    constructor(options: WebUIOptions) : super(options.context) {
        this.mOptions = options
        this.mDefaultWxOptions = createDefaultWxOptions(options)
        initWhenReady()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.mOptions = createDefaultOptions() as WebUIOptions
        this.mDefaultWxOptions = createDefaultWxOptions(mOptions)
        initWhenReady()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mOptions = createDefaultOptions() as WebUIOptions
        this.mDefaultWxOptions = createDefaultWxOptions(mOptions)
        initWhenReady()
    }

    val defaultWxOptions: WXOptions get() = mDefaultWxOptions

    @Throws(UnsupportedOperationException::class)
    private fun createDefaultWxOptions(options: WebUIOptions): WXOptions = WXOptions(
        webView = this,
        options = options
    )

    @Throws(UnsupportedOperationException::class)
    private fun createDefaultOptions(): Any {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    private fun initWhenReady() {
        // Basic setup that can run immediately
        layoutParams = LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Delay full initialization until view is properly attached
        doOnAttach {
            initJob = scope.launch {
                // Wait for first frame to ensure Activity is ready
                withContext(Dispatchers.Main) { awaitFrame() }
                initView()
            }
        }
    }

    private fun initView() {
        if (isInitialized) return

        // Window configuration
        activity.nullable {
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            if (BuildCompat.atLeastT) {
                windowInsetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        // WebView clients and settings
        webChromeClient = WXChromeClient(mOptions)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            options {
                if (debug && remoteDebug) {
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                this@apply.userAgentString = this@options.userAgentString
            }
        }

        // Background and styling
        with(mOptions) {
            setBackgroundColor(colorScheme.background.toArgb())
            background = colorScheme.background.toArgb().toDrawable()
        }

        // Window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val left = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left
            val right = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right

            val newInsets = Insets(
                top = top.asPx,
                bottom = bottom.asPx,
                left = left.asPx,
                right = right.asPx
            )

            if (mOptions.debug) Log.d(TAG, "Insets: $newInsets")

            val client = if (mOptions.client != null) {
                mOptions.client(mOptions, newInsets)
            } else {
                WXClient(mOptions, newInsets)
            }

            client.mSwipeView = mSwipeView

            super.webViewClient = client

            insets
        }

        // JavaScript interfaces (delayed until WebView is fully ready)
        post {
            addJavascriptInterfaces()
            isInitialized = true
            Log.d(TAG, "WebUI X fully initialized")
        }
    }

    private fun addJavascriptInterfaces() {
        addJavascriptInterface(
            FileInputInterface.factory(),
            ApplicationInterface.factory(),
            FileInterface.factory(),
            ModuleInterface.factory(),
            UserManagerInterface.factory(),
            PackageManagerInterface.factory(),
        )

        if (mOptions.config.dexFiles.isNotEmpty()) {
            for (dexFile in mOptions.config.dexFiles) {
                val interfaceObj = dexFile.getInterface(context, mOptions.modId)
                if (interfaceObj != null) {
                    addJavascriptInterface(interfaceObj)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
    }

    private fun cleanup() {
        initJob?.cancel()

        // remove all interfaces
        for (obj in interfaces) {
            removeJavascriptInterface(obj)
        }

        stopLoading()
        webChromeClient = null
        removeView(this)
        webChromeClient = null

        Log.d(TAG, "WebUI X cleaned up")
    }

    private val Int.asPx: Int
        get() = (this / context.resources.displayMetrics.density).toInt()

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
        if (activity == null) {
            val err = BrickException("Activity not available for postEvent")
            Log.e(TAG, err.toString())
            throwJsError(err)
            return
        }

        val adapter = moshi.adapter(WXEventHandler::class.java)

        options {
            postMessage(
                adapter.toJson(event)
            )
        }
    }

    override fun destroy() {
        cleanup()
        super.destroy()
    }

    /**
     * Loads the domain URL specified in the [WebUIOptions].
     *
     * This function checks if a new app version is required. If it is,
     * it loads a "require new version" page. Otherwise, it loads the
     * `domainUrl` from the options.
     */
    fun loadDomain() {
        options {
            if (requireNewAppVersion?.required == true) {
                loadData(
                    getRequireNewVersion(context), "text/html", "UTF-8"
                )

                return@options
            }

            loadUrl(domainUrl)
        }
    }

    @UiThread
    fun runOnUiThread(block: Activity.() -> Unit) {
        activity?.runOnUiThread {
            block(activity!!)
        } ?: throwJsError(Exception("Activity not found"))
    }

    @UiThread
    fun runJs(script: String) = runOnUiThread { evaluateJavascript(script, null) }

    @UiThread
    fun runPost(action: WXView.() -> Unit) {
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

    /**
     * Adds a JavaScript interface to this WebView.
     *
     * **Warning:** This method is deprecated for direct use on `WXView`.
     * You should use the overload `addJavascriptInterface(JavaScriptInterface)` instead,
     * which provides better type safety and integration with the **WebUI X Engine**.
     *
     * @param obj The Java object to inject into the WebView's JavaScript context.
     * @param name The name used to expose the object in JavaScript.
     * @see [android.webkit.WebView.addJavascriptInterface]
     * @see addJavascriptInterface
     */
    @SuppressLint("JavascriptInterface")
    override fun addJavascriptInterface(obj: Any, name: String) {
        interfaces += name
        super.addJavascriptInterface(obj, name)
    }

    /**
     * Adds a JavaScript interface to this WebView.
     * This method allows Java objects to be exposed to JavaScript in the WebView.
     *
     * @param obj The JavaScript interface object to add.
     *            This object must be an instance of [JavaScriptInterface].
     * @throws BrickException if there is an error adding the JavaScript interface.
     *
     * @see [android.webkit.WebView.addJavascriptInterface]
     */
    @Throws(BrickException::class)
    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(obj: JavaScriptInterface<out WXInterface>) {
        try {
            val js = obj.createNew(mDefaultWxOptions)
            addJavascriptInterface(js.instance, js.name)
        } catch (e: Exception) {
            throw BrickException(
                message = "Couldn't add a new JavaScript interface.",
                cause = e,
            )
        }
    }

    /**
     * Adds multiple JavaScript interfaces to the WebView.
     *
     * This method iterates over the provided JavaScript interfaces and adds each one individually
     * using the [addJavascriptInterface] method that accepts a single interface.
     *
     * @param obj A vararg of [JavaScriptInterface] objects to be added.
     * @throws BrickException if any error occurs during the addition of interfaces,
     *                        though this specific overload doesn't directly throw it but
     *                        the underlying single-interface method might.
     */
    @Throws(BrickException::class)
    fun addJavascriptInterface(vararg obj: JavaScriptInterface<out WXInterface>) {
        obj.forEach { addJavascriptInterface(it) }
    }

    companion object {
        private const val TAG = "WXView"
    }
}