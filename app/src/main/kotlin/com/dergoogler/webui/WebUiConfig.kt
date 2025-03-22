package com.dergoogler.webui

import com.dergoogler.mmrl.app.moshi
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.webui.model.WebUIConfig

fun webUiConfig(modId: String): WebUIConfig {
    val moduleDir = SuFile("/data/adb/modules", modId)
    val webRoot = SuFile(moduleDir, "webroot")
    val configFile = SuFile(webRoot, "config.mmrl.json")

    if (!configFile.exists()) {
        return WebUIConfig()
    }

    val json = configFile.readText()
    val jsonAdapter = moshi.adapter(WebUIConfig::class.java)
    return jsonAdapter.fromJson(json) ?: WebUIConfig()
}