package com.dergoogler.mmrl.webui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.asModId
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.confirm
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.model.Renderer
import com.dergoogler.mmrl.webui.model.WebUIConfig
import com.dergoogler.mmrl.webui.model.WebUIConfig.Companion.toWebUIConfig
import com.dergoogler.mmrl.webui.util.PostWindowEventMessage
import com.dergoogler.mmrl.webui.util.PostWindowEventMessage.Companion.asEvent
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView

open class WXActivity : ComponentActivity() {
    private var isKeyboardShowing by mutableStateOf(false)
    private lateinit var rootView: View
    private var mView: WXView? = null
    private var mOptions: WebUIOptions? = null

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
        val id = intent.fromKeys("MOD_ID", "id")
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

    open fun onRender(savedInstanceState: Bundle?): Renderer? {
        rootView = findViewById(android.R.id.content)
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerBackEvents()

        val renderer = onRender(savedInstanceState)
        if (renderer == null) throw BrickException("No WXView was passed to onRender")

        mView = renderer.view
        mOptions = renderer.options

        setContentView(mView)

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
        mView.nullable {
            val params = it.layoutParams
            params.height = rootView.height - keypadHeight
            it.layoutParams = params
        }
    }

    private fun resetWebViewHeight() {
        mView.nullable {
            val params = it.layoutParams
            params.height = LinearLayout.LayoutParams.MATCH_PARENT
            it.layoutParams = params
        }
    }

    private fun registerBackEvents() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val view = mView
                val options = mOptions

                if (view == null || options == null) {
                    Log.d(TAG, "Back pressed: view or options is null, finishing.")
                    finish()
                    return
                }

                val handler: Any? = options.config.backInterceptor ?: options.config.backHandler

                when (val backHandler = handler) {
                    is String -> when (backHandler) {
                        "native" -> handleNativeBack(view, options)
                        "javascript" -> view.postEventHandler(PostWindowEventMessage.WX_ON_BACK.asEvent)
                        else -> finish()
                    }

                    true -> handleNativeBack(view, options)
                    false, null -> finish()
                    else -> finish()
                }
            }
        })
    }

    private fun handleNativeBack(view: WXView, options: WebUIOptions) {
        if (view.canGoBack()) {
            view.goBack()
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
        super.onDestroy()

        mView.nullable {
            it.destroy()
        }
    }

    override fun onResume() {
        super.onResume()

        mView.nullable {
            it.onResume()
            it.resumeTimers()
            it.postEventHandler(PostWindowEventMessage.WX_ON_RESUME.asEvent)
        }
    }

    override fun onPause() {
        super.onPause()

        mView.nullable {
            it.onPause()
            it.pauseTimers()
            it.postEventHandler(PostWindowEventMessage.WX_ON_PAUSE.asEvent)
        }
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

    private companion object {
        const val TAG = "WXActivity"
    }
}
