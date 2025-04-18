package com.dergoogler.mmrl.webui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.webUiConfig
import java.net.URI

data class WebUIOptions(
    val modId: String,
    val appVersionCode: Int,
    val domain: String,
    val domainSafeRegex: Regex,
    val remoteDebug: Boolean,
    val debugDomainSafeRegex: Regex,
    val debug: Boolean,
    val enableEruda: Boolean,
    val debugDomain: String,
    val isDarkMode: Boolean,
    val cls: Class<*>?,
) {
    val isProviderAlive get() = Platform.isAlive

    val versionName: String
        get() = Platform.get("") {
            with(moduleManager) { version }
        }

    val versionCode: Int
        get() = Platform.get(-1) {
            with(moduleManager) { versionCode }
        }

    val platform: Platform
        get() = Platform.get(Platform.NonRoot) {
            platform
        }

    val moduleDir = SuFile("/data/adb/modules", modId)
    val webRoot = SuFile(moduleDir, "webroot")

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

    val indexFile
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

}

@Composable
/**
 * Remembers and provides the WebUI options for the current composition.
 *
 * This function is typically used in a Jetpack Compose environment to manage
 * and persist WebUI-related options across recompositions. It ensures that
 * the options are retained and accessible throughout the lifecycle of the
 * composable function.
 *
 * @return The remembered WebUI options.
 */
fun rememberWebUIOptions(
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
) = remember(
    modId,
    appVersionCode,
    domain,
    domainSafeRegex,
    debugDomainSafeRegex,
    debug,
    remoteDebug,
    enableEruda,
    debugDomain,
    isDarkMode,
    cls
) {
    WebUIOptions(
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