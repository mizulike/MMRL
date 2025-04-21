package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.webUiConfig
import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
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
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

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
 * @property backHandler Whether the WebUI should handle the back button/gesture events. Defaults to `true`.
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
    val backHandler: Boolean = true,
    val exitConfirm: Boolean = true,
    val historyFallbackFile: String = "index.html",
)
