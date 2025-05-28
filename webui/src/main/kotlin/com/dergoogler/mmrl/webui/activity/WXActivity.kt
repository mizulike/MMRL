package com.dergoogler.mmrl.webui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.asModId
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.confirm
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.model.WebUIConfig
import com.dergoogler.mmrl.webui.model.WebUIConfig.Companion.toWebUIConfig
import com.dergoogler.mmrl.webui.util.PostWindowEventMessage
import com.dergoogler.mmrl.webui.util.PostWindowEventMessage.Companion.asEvent
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView
import com.dergoogler.mmrl.webui.view.WebUIXView

/**
 * Base activity class for displaying web content using [WXView].
 *
 * This activity handles the basic lifecycle of a web view, including:
 * - Edge-to-edge display.
 * - Swipe-to-refresh functionality.
 * - Keyboard visibility adjustments.
 * - Back press handling with customizable behavior (native, JavaScript, or custom).
 * - Loading indicators.
 * - Configuration through [WebUIOptions] and [WebUIConfig].
 *
 * Subclasses should typically override [onRender] to set up their specific UI
 * and potentially provide custom loading renderers or back press handling.
 *
 * The activity expects a [ModId] to be passed via intent extras with the key "MOD_ID" or "id".
 * This [ModId] is used to load module-specific configurations.
 *
 * @property view The [WXView] instance used to display web content. Must be initialized.
 * @property options The [WebUIOptions] used to configure the web UI. Must be initialized.
 * @property modId Lazily initialized [ModId] from intent extras.
 */
open class WXActivity : ComponentActivity() {
    private var isKeyboardShowing by mutableStateOf(false)
    private lateinit var rootView: View
    private var mView: WebUIXView? = null
    private var mOptions: WebUIOptions? = null

    var view
        get() = checkNotNull(mView) {
            "mView cannot be null"
        }
        set(value) {
            mView = value
        }

    var options
        get() = checkNotNull(mOptions) {
            "mOptions cannot be null"
        }
        set(value) {
            mOptions = value
        }

    /**
     * Lazily initializes the [ModId] from the intent extras.
     * It attempts to find a string extra with the key "MOD_ID" or "id" and convert it to a [ModId].
     *
     * @return The [ModId] if found, otherwise `null`.
     */
    val modId by lazy {
        intent.fromKeys("MOD_ID", "id")
    }

    /**
     * Executes a block of code with the ModId if it exists in the intent.
     *
     * This function attempts to extract a "MOD_ID" or "id" string extra from the activity's intent.
     * If found, it converts it to a [ModId] and executes the provided [block] with the [ModId] as its receiver.
     *
     * @param R The return type of the [block].
     * @param block A lambda function that takes a [ModId] as its receiver and returns a value of type [R].
     * @return The result of executing the [block] if a [ModId] is found, otherwise `null`.
     */
    fun <R> modId(block: ModId.() -> R): R? {
        val id = modId
        if (id == null) return null
        return block(id)
    }

    /**
     * Executes the given [block] with a [WebUIConfig] instance derived from the [modId].
     *
     * This function provides a convenient way to access and configure web UI settings
     * specific to the current module identified by [modId].
     *
     * @param R The return type of the [block].
     * @param block A lambda function that receives a [WebUIConfig] and returns a value of type [R].
     * @return The result of executing the [block], or `null` if [modId] is `null`.
     */
    fun <R> config(block: WebUIConfig.() -> R): R? = modId {
        val config = toWebUIConfig()
        return@modId block(config)
    }

    /**
     * Called when the activity is ready to render its content.
     *
     * This method is responsible for initializing the `rootView` by finding it in the layout.
     * Subclasses can override this method to perform additional setup before the UI is displayed.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in [onSaveInstanceState].  **Note: Otherwise it is null.**
     */
    open fun onRender(savedInstanceState: Bundle?) {
        rootView = findViewById(android.R.id.content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onRender(savedInstanceState)
        registerBackEvents()

        config {
            if (windowResize) {
                rootView.getViewTreeObserver().addOnGlobalLayoutListener {
                    val r = Rect()
                    rootView.getWindowVisibleDisplayFrame(r)
                    val screenHeight: Int = rootView.getRootView().height
                    val keypadHeight: Int = screenHeight - r.bottom
                    if (keypadHeight > screenHeight * 0.15) {
                        if (!isKeyboardShowing) {
                            isKeyboardShowing = true
                            adjustWebViewHeight(keypadHeight)
                        }
                    } else {
                        if (isKeyboardShowing) {
                            isKeyboardShowing = false
                            resetWebViewHeight()
                        }
                    }
                }
            }
        }
    }

    private fun adjustWebViewHeight(keypadHeight: Int) {
        val params = view.layoutParams
        params.height = rootView.height - keypadHeight
        view.layoutParams = params
    }

    private fun resetWebViewHeight() {
        val params = view.layoutParams
        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        view.layoutParams = params
    }

    private fun registerBackEvents() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val handler: Any? = options.config.backInterceptor ?: options.config.backHandler

                when (val backHandler = handler) {
                    is String -> when (backHandler) {
                        "native" -> handleNativeBack()
                        "javascript" -> view.wx.postEventHandler(PostWindowEventMessage.WX_ON_BACK.asEvent)
                        else -> finish()
                    }

                    true -> handleNativeBack()
                    false, null -> finish()
                    else -> finish()
                }
            }
        })
    }

    private fun handleNativeBack() {
        if (view.wx.canGoBack()) {
            view.wx.goBack()
            return
        }

        if (options.config.exitConfirm) {
            confirm(
                confirmData = ConfirmData(
                    title = getString(R.string.exit),
                    description = getString(R.string.exit_desc),
                    onConfirm = { finish() },
                    onClose = {}
                ),
                colorScheme = options.colorScheme
            )
            return
        }

        finish()
    }

    override fun onDestroy() {
        view.wx.destroy()
        super.onDestroy()

    }

    override fun onResume() {
        with(view.wx) {
            this.onResume()
            resumeTimers()
            postEventHandler(PostWindowEventMessage.WX_ON_RESUME.asEvent)
        }

        super.onResume()
    }

    override fun onPause() {
        with(view.wx) {
            this.onPause()
            pauseTimers()
            postEventHandler(PostWindowEventMessage.WX_ON_PAUSE.asEvent)
        }

        super.onPause()
    }

    private fun Intent.fromKeys(vararg keys: String): ModId? {
        for (key in keys) {
            val extra = getStringExtra(key)
            if (extra != null) {
                return extra.asModId
            }
        }

        return null
    }

    /**
     * Creates and returns a [View] to be used as a loading indicator.
     *
     * This function constructs a [FrameLayout] that fills its parent.
     * It sets the background color based on the `options.colorScheme.background`.
     * Inside the [FrameLayout], a [ProgressBar] is added and centered.
     * The indeterminate drawable of the [ProgressBar] is tinted with the `options.colorScheme.primary` color.
     *
     * @return A [View] instance representing the loading indicator.
     */
    protected fun createLoadingRenderer(): View =
        FrameLayout(baseContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(options.colorScheme.background.toArgb())
            addView(ProgressBar(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
                indeterminateDrawable.setTint(options.colorScheme.primary.toArgb())
            })
        }

    private companion object {
        const val TAG = "WXActivity"
    }
}
