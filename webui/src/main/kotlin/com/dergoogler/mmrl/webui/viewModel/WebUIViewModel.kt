package com.dergoogler.mmrl.webui.viewModel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import com.dergoogler.mmrl.platform.Compat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.webUiConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI


@HiltViewModel(assistedFactory = WebUIViewModel.Factory::class)
class WebUIViewModel @AssistedInject constructor(
    @Assisted("modId") val modId: String,
    @Assisted("appVersionCode") val appVersionCode: Int,
    @Assisted("domain") val domain: String,
    @Assisted("domainSafeRegex") val domainSafeRegex: Regex,
    @Assisted("remoteDebug") val remoteDebug: Boolean,
    @Assisted("debugDomainSafeRegex") val debugDomainSafeRegex: Regex,
    @Assisted("debug") val debug: Boolean,
    @Assisted("enableEruda") val enableEruda: Boolean,
    @Assisted("debugDomain") val debugDomain: String,
    @Assisted("isDarkMode") val isDarkMode: Boolean,
    @Assisted("cls") val cls: Class<*>?,
    application: Application,
) : AndroidViewModel(
    application,
) {
    val isProviderAlive get() = Compat.isAlive

    val versionName: String
        get() = Compat.get("") {
            with(moduleManager) { version }
        }

    val versionCode: Int
        get() = Compat.get(-1) {
            with(moduleManager) { versionCode }
        }

    val platform: Platform
        get() = Compat.get(Platform.NonRoot) {
            platform
        }

    private val moduleDir = SuFile("/data/adb/modules", modId)
    val webRoot = SuFile(moduleDir, "webroot")

    private val configFile = SuFile(webRoot, "config.mmrl.json")
    private val pluginDir = SuFile(webRoot, "plugins")

    val sanitizedModId: String
        get() {
            return modId.replace(Regex("[^a-zA-Z0-9._]"), "_")
        }

    val sanitizedModIdWithFile
        get(): String {
            return "$${
                when {
                    sanitizedModId.length >= 2 -> sanitizedModId[0].uppercase() + sanitizedModId[1]
                    sanitizedModId.isNotEmpty() -> sanitizedModId[0].uppercase()
                    else -> ""
                }
            }File"
        }

    val sanitizedModIdWithFileInputStream = "${sanitizedModIdWithFile}InputStream"

    fun isDomainSafe(domain: String): Boolean {
        if (debug) {
            return debugDomain.isLocalWifiUrl()
        }

        return domainSafeRegex.matches(domain)
    }

    val config = webUiConfig(modId)

    private val indexFile
        get() = webRoot.list()
            .filter { !SuFile(it).isFile }
            .sortedWith(compareByDescending {
                when {
                    it.matches(Regex(".*\\.mmrl\\.(html|htm)\$")) -> 2
                    it.matches(Regex("index\\.(html|htm)\$")) -> 1
                    else -> 0
                }
            })
            .firstOrNull()

    val requireNewAppVersion = appVersionCode < config.require.version.required

    val domainUrl
        get(): String {
            if (debug && remoteDebug) {
                return debugDomain
            }

            return "$domain/$indexFile"
        }

    val isErudaEnabled = debug && enableEruda

    var recomposeCount by mutableIntStateOf(0)

    private fun String.isLocalWifiUrl(): Boolean {
        return try {
            val uri = URI(this)
            val host = uri.host ?: return false
            val port = uri.port

            host.matches(debugDomainSafeRegex) && (port == -1 || port in 1..65535)
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Composable
        fun factory(
            modId: String,
            appVersionCode: Int = -1,
            domain: String = "https://mui.kernelsu.org",
            domainSafeRegex: Regex = Regex("^https?://mui\\.kernelsu\\.org(/.*)?$"),
            debugDomainSafeRegex: Regex = Regex(
                "^(https?://)?(localhost|127\\.0\\.0\\.1|::1|10(?:\\.\\d{1,3}){3}|172\\.(?:1[6-9]|2\\d|3[01])(?:\\.\\d{1,3}){2}|192\\.168(?:\\.\\d{1,3}){2})(?::([0-9]{1,5}))?$"
            ),
            debug: Boolean = false,
            remoteDebug: Boolean = false,
            enableEruda: Boolean = false,
            debugDomain: String = "https://127.0.0.1:8080",
            isDarkMode: Boolean = false,
            cls: Class<*>? = null,
        ) = hiltViewModel<WebUIViewModel, Factory> { factory ->
            factory.create(
                modId = modId,
                appVersionCode = appVersionCode,
                domain = domain,
                domainSafeRegex = domainSafeRegex,
                debugDomainSafeRegex = debugDomainSafeRegex,
                debug = debug,
                remoteDebug = remoteDebug,
                enableEruda = enableEruda,
                debugDomain = debugDomain,
                isDarkMode = isDarkMode,
                cls = cls,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("modId") modId: String,
            @Assisted("appVersionCode") appVersionCode: Int,
            @Assisted("domain") domain: String,
            @Assisted("domainSafeRegex") domainSafeRegex: Regex,
            @Assisted("remoteDebug") remoteDebug: Boolean,
            @Assisted("debugDomainSafeRegex") debugDomainSafeRegex: Regex,
            @Assisted("debug") debug: Boolean,
            @Assisted("enableEruda") enableEruda: Boolean,
            @Assisted("debugDomain") debugDomain: String,
            @Assisted("isDarkMode") isDarkMode: Boolean,
            @Assisted("cls") cls: Class<*>?
        ): WebUIViewModel
    }
}


