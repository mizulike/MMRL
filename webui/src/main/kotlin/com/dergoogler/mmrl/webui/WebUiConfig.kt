package com.dergoogler.mmrl.webui

import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.model.WebUIConfig

fun webUiConfig(modId: String): WebUIConfig {
    val moduleDir = SuFile("/data/adb/modules", modId)
    val webRoot = SuFile(moduleDir, "webroot")
    val configFile = SuFile(webRoot, "config.json")

    if (!configFile.exists()) {
        return WebUIConfig()
    }

    val json = configFile.readText()
    val jsonAdapter = moshi.adapter(WebUIConfig::class.java)
    return jsonAdapter.fromJson(json) ?: WebUIConfig()
}