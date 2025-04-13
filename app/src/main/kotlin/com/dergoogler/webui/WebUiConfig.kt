package com.dergoogler.webui

import com.dergoogler.mmrl.app.moshi
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.webui.model.WebUIConfig

fun webUiConfig(modId: String): WebUIConfig {
    val moduleDir = com.dergoogler.mmrl.platform.file.SuFile("/data/adb/modules", modId)
    val webRoot = com.dergoogler.mmrl.platform.file.SuFile(moduleDir, "webroot")
    val configFile = com.dergoogler.mmrl.platform.file.SuFile(webRoot, "config.mmrl.json")

    if (!configFile.exists()) {
        return WebUIConfig()
    }

    val json = configFile.readText()
    val jsonAdapter = moshi.adapter(WebUIConfig::class.java)
    return jsonAdapter.fromJson(json) ?: WebUIConfig()
}