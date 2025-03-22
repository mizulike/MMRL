package com.dergoogler.webui.model

import com.squareup.moshi.JsonClass

object WebUIPermissions {
    const val FILESYSTEM = "webui.permission.FILESYSTEM"
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
}
