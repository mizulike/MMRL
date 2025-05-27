package com.dergoogler.mmrl.ui.activity.webui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.managerVersion
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.ui.activity.webui.interfaces.KernelSUInterface
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.webui.activity.WXActivity
import com.dergoogler.mmrl.webui.client.WXClient
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class WebUIActivity : WXActivity(), SwipeRefreshLayout.OnRefreshListener {
    @Inject
    internal lateinit var userPreferencesRepository: UserPreferencesRepository

    private val userPrefs get() = runBlocking { userPreferencesRepository.data.first() }

    private var mSwipeView: SwipeRefreshLayout? = null

    private val userAgent
        get(): String {
            val mmrlVersion = this.managerVersion.second

            val platform = Platform.get("Unknown") {
                platform.name
            }

            val platformVersion = Platform.get(-1) {
                moduleManager.versionCode
            }

            val osVersion = Build.VERSION.RELEASE
            val deviceModel = Build.MODEL

            return "MMRL/$mmrlVersion (Linux; Android $osVersion; $deviceModel; $platform/$platformVersion)"
        }

    override fun onRender(savedInstanceState: Bundle?) {
        super.onRender(savedInstanceState)
        mSwipeView = SwipeRefreshLayout(this)

        val modId = this.modId ?: throw BrickException("modId cannot be null or empty")

        val options = WebUIOptions(
            modId = modId,
            context = this,
            debug = userPrefs.developerMode,
            appVersionCode = BuildConfig.VERSION_CODE,
            remoteDebug = userPrefs.useWebUiDevUrl,
            enableEruda = userPrefs.enableErudaConsole,
            debugDomain = userPrefs.webUiDevUrl,
            userAgentString = userAgent,
            isDarkMode = userPrefs.isDarkMode(),
            colorScheme = userPrefs.colorScheme(this),
            client = ::client,
            cls = WebUIActivity::class.java
        )

        val view = WXView(options).apply {
            addJavascriptInterface(KernelSUInterface.factory())
            loadDomain()
        }

        this.options = options
        this.view = view

        val loading = createLoadingRenderer()
        setContentView(loading)

        lifecycleScope.launch {
            val isReady = withTimeoutOrNull(TIMEOUT_MILLIS) {
                while (!Platform.isAlive) delay(500)
                initPlatform(baseContext, userPrefs.workingMode.toPlatform())
            } ?: throw BrickException("Platform initialization timed out")

            if (!isReady) throw BrickException("Platform failed to initialize")

            val mainView = createMainView() ?: throw BrickException("Failed to create main view")

            setContentView(mainView)
        }
    }

    private fun client(options: WebUIOptions, insets: Insets) = object : WXClient(options, insets) {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mSwipeView.nullply {
                isRefreshing = true
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            mSwipeView.nullply {
                isRefreshing = false
            }
        }
    }

    override fun onRefresh() {
        // view.reload() somehow doesn't work
        view.loadDomain()
    }

    private fun createMainView() = mSwipeView.nullply {
        with(options.colorScheme) {
            setProgressBackgroundColorSchemeColor(surfaceColorAtElevation(1.dp).toArgb())
            setColorSchemeColors(
                primary.toArgb(),
                secondary.toArgb(),
            )
        }

        // Set up initial offset (can be updated later when insets arrive)
        var initialOffsetSet = false

        // Observe insets changes
        view.doOnAttach { attachedView ->
            ViewCompat.setOnApplyWindowInsetsListener(attachedView) { v, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

                if (!initialOffsetSet && topInset > 0) {
                    // Update the progress circle offset once we have insets
                    setProgressViewOffset(false, 0, topInset + 32)
                    initialOffsetSet = true
                    Log.d("INSETS", "Applied top inset: $topInset")
                }

                insets
            }
        }

        setOnRefreshListener(this@WebUIActivity)

        addView(view)
    }

    private fun createLoadingRenderer(): View =
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

    companion object {
        fun start(context: Context, modId: String) {
            val intent = Intent(context, WebUIActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    putExtra("MOD_ID", modId)
                }

            context.startActivity(intent)
        }
    }
}