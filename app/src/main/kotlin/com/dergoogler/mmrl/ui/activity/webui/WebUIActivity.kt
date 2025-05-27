package com.dergoogler.mmrl.ui.activity.webui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.managerVersion
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.ui.activity.webui.interfaces.KernelSUInterface
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.webui.activity.WXActivity
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
class WebUIActivity : WXActivity() {
    @Inject
    internal lateinit var userPreferencesRepository: UserPreferencesRepository

    private val userPrefs get() = runBlocking { userPreferencesRepository.data.first() }

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