package com.dergoogler.mmrl.webui.util

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.pm.PackageInfoCompat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.webui.webUiConfig
import java.net.URI

/**
 * Configuration options for the WebUI.
 *
 * This data class holds various settings and derived properties that define the behavior and environment of the WebUI.
 * It encapsulates information such as module identification, application version, domain settings, debugging flags,
 * and file paths related to the WebUI.
 *
 * @property modId The unique identifier of the module associated with this WebUI.
 * @property appVersionCode The version code of the application.
 * @property domain The base domain used for the WebUI (e.g., "example.com").
 * @property domainSafeRegex A regular expression used to validate if a domain is safe.
 * @property remoteDebug Indicates whether remote debugging is enabled.
 * @property debugDomainSafeRegex A regular expression used to validate if a debug domain is safe, typically used for local network checks.
 * @property debug Indicates whether debug mode is enabled.
 * @property enableEruda Indicates whether the Eruda console is enabled for debugging.
 * @property debugDomain The domain used for debugging, often a local network address.
 * @property isDarkMode Indicates whether the dark mode is enabled in the UI.
 * @property cls Optional class associated with WebUI.
 */
data class WebUIOptions(
    val context: Context,
    val modId: ModId,
    @Deprecated("Not used anymore")
    val appVersionCode: Int,
    val domain: String,
    val domainSafeRegex: Regex,
    val remoteDebug: Boolean,
    val debugDomainSafeRegex: Regex,
    val debug: Boolean,
    val enableEruda: Boolean,
    val debugDomain: String,
    val isDarkMode: Boolean,
    val userAgentString: String,
    val cls: Class<*>?,
) {
    private val packageManager
        get() = Platform.get(null) {
            packageManager
        }
    private val myUserId
        get() = Platform.get(null) {
            userManager.myUserId
        }

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

    val moduleDir = SuFile("/data/adb/modules", modId.id)
    val webRoot = SuFile(moduleDir, "webroot")

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
                    it.matches(Regex(".*\\.(wx|webuix)\\.(html|htm)$")) -> 3
                    it.matches(Regex(".*\\.mmrl\\.(html|htm)$")) -> 2
                    it.matches(Regex("index\\.(html|htm)$")) -> 1
                    else -> 0
                }
            })
            .firstOrNull()

    private val currentPackageInfo: PackageInfo?
        get() {
            if (packageManager == null) {
                Log.d(TAG, "packageManager is null")
                return null
            }

            if (myUserId == null) {
                Log.d(TAG, "myUserId is null")
                return null
            }

            try {
                return packageManager!!.getPackageInfo(context.packageName, 0, myUserId!!)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting package version code: ${e.message}")
                return null
            }
        }

    val requireNewAppVersion: Boolean
        get() {
            if (currentPackageInfo == null) {
                Log.d(TAG, "currentPackageInfo is null")
                return false
            }

            val packageName = currentPackageInfo!!.packageName
            val versionCode = PackageInfoCompat.getLongVersionCode(currentPackageInfo!!)

            val findPkgFromCfg = config.require.packages.find { pkg ->
                packageName in pkg.packageNames
            }

            if (findPkgFromCfg == null) {
                Log.d(TAG, "Package $packageName not found in config")
                return false
            }

            return versionCode < findPkgFromCfg.code
        }

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

    private companion object Default {
        const val TAG = "WebUIOptions"
    }
}

/**
 * Creates and remembers a [WebUIOptions] instance within a Composable function.
 *
 * This function is used to configure the settings for a web-based UI. It leverages Compose's
 * `remember` to cache the [WebUIOptions] instance across recompositions as long as the input
 * parameters remain the same.
 *
 * @param modId A unique identifier for the module using the web UI. This is a crucial parameter to track the origin.
 * @param appVersionCode The version code of the application. Defaults to -1 if not specified.
 * @param domain The base URL for the production web UI. Defaults to "https://mui.kernelsu.org".
 * @param domainSafeRegex A regular expression used to validate the production domain. Defaults to a regex matching "https://mui.kernelsu.org" and its subpaths.
 * @param debugDomainSafeRegex A regular expression used to validate debug domains (e.g., localhost, local IPs). Defaults to a regex matching common local development addresses.
 * @param debug A boolean flag indicating whether to enable debug mode. Defaults to `false`.
 * @param remoteDebug A boolean flag indicating whether to enable remote debugging. Defaults to `false`.
 * @param enableEruda A boolean flag indicating whether to enable the Eruda developer tool. Defaults to `false`.
 * @param debugDomain The base URL for the debug web UI. Defaults to "https://127.0.0.1:8080".
 * @param isDarkMode A boolean flag indicating whether to enable dark mode in the web UI. Defaults to `false`.
 * @param cls An optional Class object representing the caller class. Defaults to `null`. Primarily for internal logging/debugging and creating WebUI shortcuts.
 * @return A [WebUIOptions] instance configured with the provided parameters.
 */
@Composable
fun rememberWebUIOptions(
    modId: ModId,
    context: Context = Platform.context,
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
    userAgentString: String = "DON'T TRACK ME DOWN MOTHERFUCKER!",
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
    userAgentString,
    cls
) {
    WebUIOptions(
        context = context,
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
        userAgentString = userAgentString,
        cls = cls,
    )
}