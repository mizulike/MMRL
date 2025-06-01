package com.dergoogler.mmrl.ui.activity.webui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.platform.model.ModId.Companion.getModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.webrootDir
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.activity.webui.interfaces.KernelSUInterface
import com.dergoogler.mmrl.webui.handler.suPathHandler
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WebUIView
import com.dergoogler.mmrl.webui.wxAssetLoader
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@SuppressLint("SetJavaScriptEnabled")
class KsuWebUIActivity : MMRLComponentActivity() {
    private val userPrefs get() = runBlocking { userPreferencesRepository.data.first() }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable edge to edge
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        val modId = intent.getModId() ?: throw BrickException("Invalid Module ID")

        val webViewClient = object : WebViewClient() {
            val assetLoader = wxAssetLoader(
                handlers = listOf(
                    "/" to suPathHandler(modId.webrootDir)
                )
            )

            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest,
            ): WebResourceResponse? {
                return assetLoader(request.url)
            }
        }

        val options = WebUIOptions(
            modId = modId,
            debug = userPrefs.developerMode,
            context = this,
        )

        val webView = WebUIView(options).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams<MarginLayoutParams> {
                    leftMargin = inset.left
                    rightMargin = inset.right
                    topMargin = inset.top
                    bottomMargin = inset.bottom
                }
                return@setOnApplyWindowInsetsListener insets
            }

            val factory = KernelSUInterface.factory()

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            addJavascriptInterface(factory)
            setWebViewClient(webViewClient)
            loadDomain()
        }

        setContentView(webView)
    }
}