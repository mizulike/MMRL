package com.dergoogler.mmrl.ui.activity.webui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.ext.managerVersion
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.model.ModId.Companion.isNullOrEmpty
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.ui.activity.webui.interfaces.KernelSUInterface
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.webui.activity.WXActivity
import com.dergoogler.mmrl.webui.model.Renderer
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    override fun onRender(savedInstanceState: Bundle?): Renderer? {
        super.onRender(savedInstanceState)

        lifecycleScope.launch {
            initPlatform(baseContext, userPrefs.workingMode.toPlatform())
        }

        if (modId.isNullOrEmpty()) {
            return null
        }

        val options by lazy {
            WebUIOptions(
                modId = modId!!,
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
        }

        val view = WXView(options).apply {
            addJavascriptInterface(KernelSUInterface.factory())
            loadDomain()
        }

        return Renderer(
            view = view,
            options = options
        )
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