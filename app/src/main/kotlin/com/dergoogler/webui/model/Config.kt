package com.dergoogler.webui.model

import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val FILESYSTEM = "webui.permission.FILESYSTEM"
    const val ERUDA = "webui.permission.ERUDA"
    const val PLUGIN_DEX_LOADER = "webui.permission.PLUGIN_DEX_LOADER"
}

@JsonClass(generateAdapter = true)
data class WebUIConfigRequireVersion(
    val required: Int = 0,
    val supportText: String? = null,
    val supportLink: String? = null,
)

@JsonClass(generateAdapter = true)
data class WebUIConfigRequire(
    val version: WebUIConfigRequireVersion = WebUIConfigRequireVersion(),
)

@JsonClass(generateAdapter = true)
data class WebUIConfig(
    val plugins: List<String> = emptyList(),
    val require: WebUIConfigRequire = WebUIConfigRequire(),
    val permissions: List<String> = emptyList(),
) {
    val hasFileSystemPermission = permissions.contains(WebUIPermissions.FILESYSTEM)
    val hasErudaPermission = permissions.contains(WebUIPermissions.ERUDA)
    val hasPluginDexLoaderPermission = permissions.contains(WebUIPermissions.PLUGIN_DEX_LOADER)
}
