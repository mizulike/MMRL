package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.webUiConfig
import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
}

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    /**
     * Specifies the required value for this configuration.
     * Default value is set to 1.
     */
    val required: Int = 1,
    /**
     * Optional text providing additional support information or guidance.
     * This can be null if no support text is provided.
     */
    val supportText: String? = null,
    /**
     * A nullable string representing the support link.
     * This can be used to provide a URL or contact information for support purposes.
     * If null, no support link is provided.
     */
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    /**
     * Represents the required version configuration for the Web UI.
     * This property holds an instance of [WebUIConfigRequireVersion], which specifies
     * the version requirements for the Web UI to function properly.
     */
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = true)
data class WebUIConfig(
    /**
     * Specifies the required configuration settings for the WebUI.
     * This property holds an instance of [WebUIConfigRequire], which defines
     * the mandatory parameters needed for the WebUI to function correctly.
     */
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
    /**
     * Indicates whether the application should fall back to using history
     * in case of certain failures or conditions. When set to `true`, the
     * application will attempt to use historical data as a fallback mechanism.
     * Defaults to `false`.
     */
    val historyFallback: Boolean = false,
    /**
     * The title of the configuration. This is an optional property and can be null.
     */
    val title: String? = null,
    /**
     * The icon associated with the configuration. This is an optional property
     * and can be null if no icon is specified.
     */
    val icon: String? = null,
    /**
     * Indicates whether the application window can be resized.
     * If set to `true`, the window resizing feature is enabled.
     */
    val windowResize: Boolean = true,
    /**
     * Indicates whether the back button handler is enabled or not.
     * When set to `true`, the application will handle back button presses.
     */
    val backHandler: Boolean = true,
    /**
     * Indicates whether a confirmation dialog should be displayed before exiting the application.
     * If set to `true`, the user will be prompted to confirm their action before the application exits.
     */
    val exitConfirm: Boolean = true,
    /**
     * The name of the fallback file to be used for history-based routing.
     * Typically, this is used in single-page applications (SPAs) to serve
     * a default file (e.g., "index.html") when a route does not match
     * any static files.
     */
    val historyFallbackFile: String = "index.html",
) {
    companion object {
        fun String.toWebUiConfig() = webUiConfig(this)
    }
}
