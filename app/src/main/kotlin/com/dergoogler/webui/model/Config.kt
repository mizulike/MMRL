package com.dergoogler.webui.model

import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val FILESYSTEM = "webui.permission.FILESYSTEM"
    const val KERNELSU_WEBUI_API = "kernelsu.permission.WEBUI_API"
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
    const val DSL_DEX_LOADING = "webui.permission.DSL_DEX_LOADING"
}

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    val required: Int = 1,
    val supportText: String? = null,
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = true)
data class WebUIConfigDsl(
    val path: String? = null,
    val className: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfig(
    val dsl: WebUIConfigDsl = WebUIConfigDsl(),
    val plugins: List<String> = emptyList(),
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
    val historyFallback: Boolean = false,
    val historyFallbackFile: String = "index.html",
) {
    val hasFileSystemPermission = permissions.contains(WebUIPermissions.FILESYSTEM)
    val hasPluginDexLoaderPermission = permissions.contains(WebUIPermissions.PLUGIN_DEX_LOADER)
    val hasDslDexLoadingPermission = permissions.contains(WebUIPermissions.DSL_DEX_LOADING)
    val hasKernelSuWebUiApiPermission = permissions.contains(WebUIPermissions.KERNELSU_WEBUI_API)
}
