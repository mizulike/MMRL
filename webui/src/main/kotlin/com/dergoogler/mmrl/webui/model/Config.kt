package com.dergoogler.mmrl.webui.model

import android.content.Context
import android.util.Log
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.webUiConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dalvik.system.InMemoryDexClassLoader
import java.nio.ByteBuffer

object WebUIPermissions {
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
    const val WX_ROOT_PATH = "wx.permission.ROOT_PATH"
}

/**
 * Represents the required version information for interacting with the Web UI.
 *
 * This data class specifies the minimum version of the Web UI that the client must be using,
 * along with optional supporting text and a link for the user to get help or updates.
 *
 * @property required The minimum required version number (an integer). Defaults to 1.
 *                    Clients with a Web UI version lower than this value should be prompted to upgrade.
 * @property supportText Optional text providing additional context or instructions to the user.
 *                       For example: "Please update to the latest version for the best experience."
 * @property supportLink Optional URL link where the user can find more information about the
 *                       required version, such as download instructions or release notes.
 *                       For example: "https://example.com/webui-update"
 */
@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    val required: Int = 1,
    val supportText: String? = null,
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersionPackages(
    val code: Int = -1,
    val packageName: Any,
    val supportText: String? = null,
    val supportLink: String? = null,
) {
    val packageNames
        get(): List<String> {
            return when (packageName) {
                is String -> listOf(packageName)
                is List<*> -> packageName.filterIsInstance<String>()
                else -> emptyList()
            }
        }
}

/**
 * Represents the required configuration for the Web UI.
 *
 * This data class defines the minimum required configuration settings needed for the Web UI to function correctly.
 * Currently, it only includes the required version information.
 *
 * @property version The required version details for the Web UI. Defaults to a new [WebUIConfigRequireVersion] instance.
 */
@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    val packages: List<WebUIConfigRequireVersionPackages> = emptyList(),
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = true)
data class WebUIConfigDexFile(
    val path: String? = null,
    val className: String? = null,
) {
    private companion object {
        const val TAG = "WebUIConfigDexFile"
    }

    fun getInterface(context: Context, modId: ModId): JavaScriptInterface<out WXInterface>? {
        if (path == null || className == null) {
            return null
        }

        val file = SuFile("/data/adb/modules", modId.id, "webroot", path)

        if (!file.isFile) {
            return null
        }

        if (file.extension != "dex") {
            return null
        }

        try {
            val dexFileParcel = file.readBytes()
            val loader =
                InMemoryDexClassLoader(
                    ByteBuffer.wrap(dexFileParcel),
                    context.classLoader
                )

            val rawClass = loader.loadClass(className)
            if (!WXInterface::class.java.isAssignableFrom(rawClass)) {
                Log.e(TAG, "Loaded class $className does not implement WXInterface")
                return null
            }

            @Suppress("UNCHECKED_CAST")
            val clazz = rawClass as Class<out WXInterface>
            return JavaScriptInterface(clazz)
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Class $className not found in dex file ${file.path}", e)
            return null
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error instantiating class $className from dex file ${file.path}",
                e
            )
            return null
        }
    }
}


/**
 * Configuration class for the WebUI.
 *
 * This data class defines the configuration options for how the WebUI should behave and appear.
 * It includes settings for required dependencies, permissions, history handling,
 * window behavior, and more.
 *
 * @property require Configuration for required dependencies for the WebUI. Defaults to an empty [WebUIConfigRequire].
 * @property permissions List of permissions required by the Web UI. Defaults to an empty list.
 * @property historyFallback Whether the WebUI should fallback to the `historyFallbackFile` if a route is not found. Defaults to `false`.
 * @property title The title of the WebUI window. If null, the default title of the underlying platform will be used. Defaults to `null`.
 * @property icon The path or URL to the icon of the WebUI. If null, the default icon of the underlying platform will be used. Defaults to `null`.
 * @property windowResize Whether the WebUI window should be resizable. Defaults to `true`.
 * @property backHandler Whether the WebUI should handle the back button/gesture events. Requires [backEvent] to be `false`. Defaults to `true`.
 * @property backEvent Whether the WebUI should handle the back button/gesture events via JavaScript. Requires [backHandler] to be `false`. Defaults to `false`.
 * @property exitConfirm Whether the WebUI should show a confirmation dialog when the user tries to exit. Defaults to `true`.
 * @property historyFallbackFile The file to use as a fallback when `historyFallback` is enabled. Defaults to "index.html".
 */
@JsonClass(generateAdapter = true)
data class WebUIConfig(
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
    val historyFallback: Boolean = false,
    val title: String? = null,
    val icon: String? = null,
    val windowResize: Boolean = true,
    val backHandler: Any? = true,
    val exitConfirm: Boolean = true,
    val historyFallbackFile: String = "index.html",
    val autoStatusBarsStyle: Boolean = true,
    val dexFiles: List<WebUIConfigDexFile> = emptyList(),
) {
    val hasRootPathPermission get() = WebUIPermissions.WX_ROOT_PATH in permissions

    companion object {
        /**
         * Converts a [ModId] to a [webUiConfig] string representation.
         *
         * This function takes a [ModId] and returns a string that represents the
         * configuration for the web UI, specifically utilizing the provided Mod ID.
         * It's a concise way to generate the configuration based solely on the Mod ID.
         *
         * @receiver The [ModId] to convert.
         * @return A string representing the web UI configuration for the given [ModId].
         * @see webUiConfig
         *
         * @sample
         * ```kotlin
         * val modId = ModId("my-mod")
         * val webUiConfigString = modId.toWebUIConfig()
         * println(webUiConfigString) // Output will be "webUiConfig(ModId(id=my-mod))"
         * ```
         */
        fun ModId.toWebUIConfig() = webUiConfig(this)
    }
}
