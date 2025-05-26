package com.dergoogler.mmrl.webui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup.LayoutParams
import android.view.WindowInsetsController
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.UiThread
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import com.dergoogler.mmrl.ext.BuildCompat
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
import com.dergoogler.mmrl.webui.util.PostWindowEventMessage
import com.dergoogler.mmrl.webui.util.WebUIOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
open class WXView : WebView {
    private val mOptions: WebUIOptions
    private val activity: Activity? by lazy { mOptions.findActivity() }
    private val scope = CoroutineScope(Dispatchers.Main)
    private var initJob: Job? = null
    private var isInitialized = false

    private var storedInterfaces: MutableList<JavaScriptInterface.Instance> = mutableListOf()

    constructor(options: WebUIOptions) : super(options.context) {
        this.mOptions = options
        initWhenReady()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.mOptions = createDefaultOptions() as WebUIOptions
        initWhenReady()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mOptions = createDefaultOptions() as WebUIOptions
        initWhenReady()
    }

    @Throws(UnsupportedOperationException::class)
    private fun createDefaultOptions(): Any {
        throw UnsupportedOperationException("Default constructor not supported. Use constructor with options.")
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
            webViewClient = WXClient(mOptions, newInsets)
            insets
        }

        // JavaScript interfaces (delayed until WebView is fully ready)
        post {
            addJavascriptInterfaces()
            isInitialized = true
            Log.d(TAG, "WebView fully initialized")
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
        super.onDetachedFromWindow()
        cleanup()
    }

    private fun cleanup() {
        initJob?.cancel()

        // remove all interfaces
        for (jsInterface in storedInterfaces) {
            removeJavascriptInterface(jsInterface.name)
        }

        webChromeClient = null
        Log.d(TAG, "WebView cleaned up")
    }

    private val Int.asPx: Int
        get() = (this / context.resources.displayMetrics.density).toInt()

    companion object {
        private const val TAG = "WXView"
    }

    fun <R> options(block: WebUIOptions.() -> R): R? {
        return try {
            block(mOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get options:", e)
            null
        }
    }

    fun postEvent(event: PostWindowEventMessage) {
        if (activity == null) {
            val err = Exception("Activity not available for postEvent")
            Log.e(TAG, err.toString())
            throwJsError(err)
            return
        }

        options {
            postWebMessage(event.message, "*".toUri())
        }
    }


    override fun destroy() {
        super.destroy()
        cleanup()
    }

    override fun onResume() {
        super.onResume()
        postEvent(PostWindowEventMessage.ON_VIEW_RESUME)
    }

    override fun onPause() {
        super.onPause()
        postEvent(PostWindowEventMessage.ON_VIEW_PAUSE)
    }

    fun loadDomain() {
        options {
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

    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(jsInterface: JavaScriptInterface<out WXInterface>) {
        val js = jsInterface.createNew(
            wxOptions = WXOptions(
                webView = this,
                options = mOptions
            )
        )
        storedInterfaces += js
        addJavascriptInterface(js.instance, js.name)
    }

    fun addJavascriptInterface(vararg jsInterfaces: JavaScriptInterface<out WXInterface>) {
        jsInterfaces.forEach { addJavascriptInterface(it) }
    }
}